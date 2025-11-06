package cn.civer.blog.Service.Impl;

import cn.civer.blog.Mapper.*;
import cn.civer.blog.Model.DTO.*;
import cn.civer.blog.Model.Entity.*;
import cn.civer.blog.Service.CategoryServ;
import cn.civer.blog.Utils.PostAssembler;
import cn.civer.blog.Service.LabelServ;
import cn.civer.blog.Service.PostServ;
import cn.civer.blog.Utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.context.annotation.Lazy;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PostServImpl implements PostServ {
    @Autowired
    private LabelServ labelServ;
    @Autowired
    private CategoryServ categoryServ;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private PostLabelMapper postLabelMapper;
    @Autowired
    private PostCategoryMapper postCategoryMapper;
    @Autowired
    private PostAssembler postAssembler;
    @Autowired
    private RedisUtils redisUtils;

    // 注入接口类型的代理引用，用于触发 Spring 的缓存切面（避免自调用忽略缓存）
    @Autowired
    @Lazy
    private PostServ postServProxy;

    /**
     * 获取当前用户Id
     *
     * @return 用户Id
     */
    private BigInteger getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new BigInteger(authentication.getName());
    }

    /**
     * 新增文章
     *
     * @param postDTO 文章DTO
     * @return 插入结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postAdd(PostDTO postDTO) {
        // 获取用户Id
        BigInteger authorId = getCurrentUserId();
        Post post = new Post();
        // 1️⃣ 更新主表
        // 1.1 设置作者
        post.setAuthorId(authorId);
        // 1.2 插入文章表并获取文章ID
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setSummary(postDTO.getSummary());
        postMapper.insert(post);
        BigInteger postId = post.getId();
        // 将新文章写入 postsById 缓存（局部缓存填充，避免后续立刻穿透）
        postAssembler.enrichPost(post);
        log.info(MessageConstants.POST_ADD_SUCCESS + "：[{}] (ID: {})", post.getTitle(), postId);

        // 2️⃣ 分类处理
        for (CategoryDTO c : postDTO.getCategories()) {
            Category category = categoryServ.findOrCreate(c.getTitle(), authorId);
            log.info("准备插入文章分类映射：postId={}, categoryId={}", postId, category.getId());
            postCategoryMapper.insertIfNotExist(postId, category.getId());
            log.info(MessageConstants.POST_CATEGORY_INSERT_SUCCESS+": 绑定分类 [{}] -> 文章 [{}]", category.getTitle(), post.getTitle());
        }

        // 3️⃣ 标签处理
        for (LabelDTO l : postDTO.getLabels()) {
            Label label = labelServ.findOrCreate(l.getTitle(), authorId);
            log.info("准备插入文章标签映射：postId={}, labelId={}", postId, label.getId());
            postLabelMapper.insertIfNotExist(postId, label.getId());
            log.info(MessageConstants.POST_LABEL_INSERT_SUCCESS+": 绑定标签 [{}] -> 文章 [{}]", label.getTitle(), post.getTitle());
        }

        return Boolean.TRUE;
    }

    /**
     * 更新文章
     *
     * @param postId  文章Id
     * @param postDTO 文章DTO
     * @return 修改结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postUpdate(BigInteger postId, PostDTO postDTO) {
        // 获取用户Id
        BigInteger authorId = getCurrentUserId();
        // 1️⃣ 更新主表
        Post post = new Post();
        post.setId(postId);

        if (StringUtils.hasText(postDTO.getTitle()))
            post.setTitle(postDTO.getTitle());
        if (StringUtils.hasText(postDTO.getSummary()))
            post.setSummary(postDTO.getSummary());
        if (StringUtils.hasText(postDTO.getContent()))
            post.setContent(postDTO.getContent());
        if (postDTO.getStatus() != null)
            post.setStatus(postDTO.getStatus());

        postMapper.update(post);

        // 读取更新后的文章并写回 postsById 缓存（比简单 evict 更友好）
        Post updated = postMapper.selectById(postId);
        if (updated != null) {
            postAssembler.enrichPost(updated);
        }

        // 2️⃣ 重建分类关联
        if (!postDTO.getCategories().isEmpty()) {
            postCategoryMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS);
            for (CategoryDTO c : postDTO.getCategories()) {
                Category category = categoryServ.findOrCreate(c.getTitle(), authorId);
                postCategoryMapper.insertIfNotExist(postId, category.getId());
                log.info(MessageConstants.POST_CATEGORY_INSERT_SUCCESS+": 绑定分类 [{}] -> 文章 [{}]", category.getTitle(), post.getTitle());
            }
        }

        // 3️⃣ 重建标签关联
        if (!postDTO.getLabels().isEmpty()) {
            postLabelMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS);
            for (LabelDTO l : postDTO.getLabels()) {
                Label label = labelServ.findOrCreate(l.getTitle(), authorId);
                postLabelMapper.insertIfNotExist(postId, label.getId());
                log.info(MessageConstants.POST_LABEL_INSERT_SUCCESS+": 绑定标签 [{}] -> 文章 [{}]", label.getTitle(), post.getTitle());
            }
        }
        log.info(MessageConstants.POST_UPDATE_SUCCESS + " (ID:{}) ", postId);
        return Boolean.TRUE;
    }

    /**
     * 根据文章ID删除文章
     *
     * @param postId 文章Id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postDeleteById(BigInteger postId) {
        // 每次调用方法时动态获取
        BigInteger authorId = getCurrentUserId();
        // 读取文章标题以便针对性驱逐 postsByTitle
        Post toDeletePost = postMapper.selectById(postId);
        // 1. 先收集与文章关联的分类/标签（用于驱逐相关缓存）
        List<BigInteger> categoryIds = postCategoryMapper.selectByPostId(postId);
        List<BigInteger> labelIds = postLabelMapper.selectByPostId(postId);
        // 2. 文章删除成功
        if (postMapper.deleteById(postId) == 1) {
            log.info("文章(ID:{})已被 (用户:{}) 删除", postId, authorId);
            // 3. 删除对应的文章-标签表/分类表
            postLabelMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS);
            postCategoryMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS);

        }
        return Boolean.TRUE;
    }

    /**
     * 根据分类删除文章
     *
     * @param categoryId 分类
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postDeleteByCategory(BigInteger categoryId) {
        // 1. 查询包含该分类Id的文章
        List<BigInteger> postIds = postCategoryMapper.selectByCategoryId(categoryId);

        // 读取受影响文章标题以便驱逐 postsByTitle
        List<String> affectedTitles = new ArrayList<>();
        for (BigInteger pid : postIds) {
            Post p = postMapper.selectById(pid);
            if (p != null && p.getTitle() != null) {
                affectedTitles.add(p.getTitle());
            }
        }
        // 2. 遍历postIds
        for (BigInteger postId : postIds) {
            // 3. 根据Id删除文章
            if (postMapper.deleteById(postId) == 1) {
                log.info(MessageConstants.POST_DELETE_SUCCESS+": 文章ID({}) ",postId);
            }
            // 4. 根据Id删除文章-分类表
            if (postCategoryMapper.deleteByPostId(postId) == 1) {
                log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS + ": 文章ID({})", postId);
            }
            // 5. 根据Id删除文章-标签表
            if (postLabelMapper.deleteByPostId(postId) == 1) {
                log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS+": 文章ID({})", postId);
            }

        }

        return Boolean.TRUE;
    }

    /**
     * 根据用户Id删除文章
     *
     * @param userId 用户Id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean postDeleteByUserId(BigInteger userId) {
        List<Post> posts = postMapper.selectByAuthorId(userId);
        List<String> affectedTitles = new ArrayList<>();
        for (Post post : posts) {
            // 1. 文章删除成功
            if (postMapper.deleteById(post.getId()) == 1) {
                log.info(MessageConstants.POST_DELETE_SUCCESS+": 文章(ID:{})", post.getId());
                // 2. 删除对应的文章-标签表/分类表
                postLabelMapper.deleteByPostId(post.getId());
                log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS+": 文章ID({})", post.getId());
                postCategoryMapper.deleteByPostId(post.getId());
                log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS + ": 文章ID({})", post.getId());

                if (post.getTitle() != null) affectedTitles.add(post.getTitle());
            } else {
                // 文章删除失败
                log.warn(MessageConstants.POST_DELETE_FAILED+": 文章(ID:{})" , post.getId());
            }
        }

        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postDeleteByLabel(BigInteger labelId) {
        // 1. 查询包含该分类Id的文章
        List<BigInteger> postIds = postLabelMapper.selectBylabelId(labelId);

        // 读取受影响文章标题以便驱逐 postsByTitle
        List<String> affectedTitles = new ArrayList<>();
        for (BigInteger pid : postIds) {
            Post p = postMapper.selectById(pid);
            if (p != null && p.getTitle() != null) affectedTitles.add(p.getTitle());
        }
        // 2. 遍历postIds
        for (BigInteger postId : postIds) {
            // 3. 根据Id删除文章
            if (postMapper.deleteById(postId) == 1) {
                log.info(MessageConstants.POST_DELETE_SUCCESS+": 文章ID({}) ",postId);
            }
            // 4. 根据Id删除文章-分类表
            if (postCategoryMapper.deleteByPostId(postId) == 1) {
                log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS + ": 文章ID({})", postId);
            }
            // 5. 根据Id删除文章-标签表
            if (postLabelMapper.deleteByPostId(postId) == 1) {
                log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS+": 文章ID({})", postId);
            }

        }

        return Boolean.TRUE;
    }

    /*--------------------查询部分------------------------------------------------------*/

    /**
     * 查询所有文章
     *
     * @return 文章列表
     */
    @Override
    public List<Post> postSelectAll() {
        List<Post> posts = postMapper.selectAll();
        postAssembler.enrichPosts(posts);
        log.info(MessageConstants.POST_SELECT_SUCCESS);
        return posts;
    }

    @Override
    public Boolean postIncreLikes(BigInteger postId) {
//        postMapper.incrementLikes(postId);
//        redisUtils.increZSetScore(MessageConstants.REDIS_POST_LIKES,postId.toString(),1L);
        // 写入 Redis 中的有序集合以记录增量（不立即写库）
        redisUtils.increZSetScore(MessageConstants.REDIS_POST_LIKES, postId.toString(), 1L);
        log.info(MessageConstants.POST_LIKE_SUCCESS+" (ID:{})", postId);
        return Boolean.TRUE;
    }

    @Override
    public Boolean postIncreViews(BigInteger postId) {
//        postMapper.incrementViews(postId);
//        redisUtils.increZSetScore(MessageConstants.REDIS_POST_VIEWS,postId.toString(),1L);
        // 写入 Redis 有序集合记录浏览量增量（不立即写库）
        redisUtils.increZSetScore(MessageConstants.REDIS_POST_VIEWS, postId.toString(), 1L);
        log.info(MessageConstants.POST_VIEW_SUCCESS+" (ID:{})", postId);
        return Boolean.TRUE;
    }

    /**
     * 根据id查找文章
     *
     * @param postId 文章id
     * @return 查找结果
     */
    @Override
    public Post postSelectById(BigInteger postId) {
        // 设置文章其他信息
        Post post = postMapper.selectById(postId);
        if (post == null)
            return null;
        postAssembler.enrichPost(post);
        log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postId);
        return post;
    }

    /**
     * 根据标题查找文章
     *
     * @param title 文章名
     * @return 查找结果
     */
    @Override
    public List<Post> postSelectByTitle(String title) {
        List<Post> posts = postMapper.selectByTitle(title);
        postAssembler.enrichPosts(posts);
        log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章名: {}",title);
        return posts;
    }

    /* ************************* 优化点：按分类/标签查询改为缓存文章ID列表 *************************
     * 说明：将分类/标签到文章ID的映射缓存（轻量），并复用按ID缓存（postsById）来加载每篇文章，
     * 避免把同一篇文章对象在多个集合缓存中重复存储，降低缓存失效的范围及存储成本。
     */

    /**
     * 根据分类查询文章ID（缓存轻量的ID列表）
     *
     * @param categoryId 分类Id
     * @return 文章ID列表
     */
    public List<BigInteger> postIdsByCategory(BigInteger categoryId) {
        return postCategoryMapper.selectByCategoryId(categoryId);
    }

    /**
     * 根据分类查询文章
     *
     * @param categoryId 分类Id
     * @return 查询文章的集合
     */
    @Override
    public List<Post> postSelectByCategory(BigInteger categoryId) {
        // 通过代理调用带缓存的方法以保证缓存生效
        List<BigInteger> postIds = postServProxy.postIdsByCategory(categoryId);
        log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postIds);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postServProxy.postSelectById(postId); // 利用 postsById 缓存（通过代理触发）
            if (post != null) {
                posts.add(post);
                log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postId);
            }
        }
        return posts;
    }

    /**
     * 根据标签查询文章ID（缓存轻量的ID列表）
     *
     * @param labelId 标签
     * @return 文章ID列表
     */
    public List<BigInteger> postIdsByLabel(BigInteger labelId) {
        return postLabelMapper.selectBylabelId(labelId);
    }

    /**
     * 根据标签查询文章
     *
     * @param labelId 标签
     * @return 查询文章的集合
     */
    @Override
    public List<Post> postSelectByLabel(BigInteger labelId) {
        // 通过代理调用带缓存的方法以保证缓存生效
        List<BigInteger> postIds = postServProxy.postIdsByLabel(labelId);
        log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postIds);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postServProxy.postSelectById(postId); // 利用 postsById 缓存（通过代理触发）
            if (post != null) {
                posts.add(post);
                log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postId);
            }
        }
        return posts;
    }
}
