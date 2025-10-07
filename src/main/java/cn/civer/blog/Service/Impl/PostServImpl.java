package cn.civer.blog.Service.Impl;

import cn.civer.blog.Mapper.*;
import cn.civer.blog.Model.DTO.*;
import cn.civer.blog.Model.Entity.*;
import cn.civer.blog.Service.CategoryServ;
import cn.civer.blog.Utils.PostAssembler;
import cn.civer.blog.Service.LabelServ;
import cn.civer.blog.Service.PostServ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        log.info(MessageConstants.POST_ADD_SUCCESS + "：[{}] (ID: {})", post.getTitle(), postId);

        // 2️⃣ 分类处理
        for (CategoryDTO c : postDTO.getCategories()) {
            Category category = categoryServ.findOrCreate(c.getTitle(), c.getSummary(), authorId);
            log.info("准备插入文章分类映射：postId={}, categoryId={}", postId, category.getId());
            postCategoryMapper.insertIfNotExist(postId, category.getId());
            log.info(MessageConstants.POST_CATEGORY_INSERT_SUCCESS+": 绑定分类 [{}] -> 文章 [{}]", category.getTitle(), post.getTitle());
        }

        // 3️⃣ 标签处理
        for (LabelDTO l : postDTO.getLabels()) {
            Label label = labelServ.findOrCreate(l.getTitle(), l.getSummary(), authorId);
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

        // 2️⃣ 重建分类关联
        if (!postDTO.getCategories().isEmpty()) {
            postCategoryMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS);
            for (CategoryDTO c : postDTO.getCategories()) {
                Category category = categoryServ.findOrCreate(c.getTitle(), c.getSummary(), authorId);
                postCategoryMapper.insertIfNotExist(postId, category.getId());
                log.info(MessageConstants.POST_CATEGORY_INSERT_SUCCESS+": 绑定分类 [{}] -> 文章 [{}]", category.getTitle(), post.getTitle());
            }
        }

        // 3️⃣ 重建标签关联
        if (!postDTO.getLabels().isEmpty()) {
            postLabelMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS);
            for (LabelDTO l : postDTO.getLabels()) {
                Label label = labelServ.findOrCreate(l.getTitle(), l.getSummary(), authorId);
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
     * @param id 文章Id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postDeleteById(BigInteger id) {
        // 每次调用方法时动态获取
        BigInteger authorId = getCurrentUserId();
        // 1. 文章删除成功
        if (postMapper.deleteById(id) == 1) {
            log.info("文章(ID:{})已被 (用户:{}) 删除", id, authorId);
            // 2. 删除对应的文章-标签表/分类表
            postLabelMapper.deleteByPostId(id);
            log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS);
            postCategoryMapper.deleteByPostId(id);
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
        for (Post post : posts) {
            // 1. 文章删除成功
            if (postMapper.deleteById(post.getId()) == 1) {
                log.info(MessageConstants.POST_DELETE_SUCCESS+": 文章(ID:{})", post.getId());
                // 2. 删除对应的文章-标签表/分类表
                postLabelMapper.deleteByPostId(post.getId());
                log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS+": 文章ID({})", post.getId());
                postCategoryMapper.deleteByPostId(post.getId());
                log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS + ": 文章ID({})", post.getId());
            } else {
                // 文章删除失败
                log.warn(MessageConstants.POST_DELETE_FAILED+": 文章(ID:{})" , post.getId());
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 根据标签删除文章
     *
     * @param labelId 标签
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postDeleteByLabel(BigInteger labelId) {
        // 1. 查询包含该分类Id的文章
        List<BigInteger> postIds = postLabelMapper.selectBylabelId(labelId);
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
//        if (posts.isEmpty())
//            return Result.success("无文章");
        postAssembler.enrichPosts(posts);
        log.info(MessageConstants.POST_SELECT_SUCCESS);
        return posts;
    }

    @Override
    public Boolean postIncreLikes(BigInteger postId) {
        postMapper.incrementLikes(postId);
        return Boolean.TRUE;
    }

    @Override
    public Boolean postIncreViews(BigInteger postId) {
        postMapper.incrementViews(postId);
        return Boolean.TRUE;
    }

    /**
     * 根据id查找文章
     *
     * @param id 文章id
     * @return 查找结果
     */
    @Override
    public Post postSelectById(BigInteger id) {
        // 设置文章其他信息
        Post post = postMapper.selectById(id);
        if (post == null)
            return null;
        postAssembler.enrichPost(post);
        log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",id);
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

    /**
     * 根据分类查询文章
     *
     * @param categoryId 分类Id
     * @return 查询文章的集合
     */
    @Override
    public List<Post> postSelectByCategory(BigInteger categoryId) {
        List<BigInteger> postIds = postCategoryMapper.selectByCategoryId(categoryId);
        log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postIds);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postMapper.selectById(postId);
            postAssembler.enrichPost(post);
            posts.add(post);
            log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postId);
        }
        return posts;
    }

    /**
     * 根据标签查询文章
     *
     * @param labelId 标签
     * @return 查询文章的集合
     */
    @Override
    public List<Post> postSelectByLabel(BigInteger labelId) {
        List<BigInteger> postIds = postLabelMapper.selectBylabelId(labelId);
        log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postIds);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postMapper.selectById(postId);
            postAssembler.enrichPost(post);
            posts.add(post);
            log.info(MessageConstants.POST_SELECT_SUCCESS+": 文章ID: {}",postId);
        }
        return posts;
    }
}
