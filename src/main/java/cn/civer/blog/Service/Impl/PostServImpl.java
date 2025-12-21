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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.context.annotation.Lazy;
import cn.civer.blog.Exception.BizException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new BizException("无法获取当前用户信息");
        }
        return new BigInteger(authentication.getName());
    }
    
    /**
     * 检查当前用户是否是管理员
     * @return 是否是管理员
     */
    private boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_manager"));
    }

    /**
     * 新增文章
     *
     * @param postDTO 文章DTO
     * @return 插入结果
     */
//    @CacheEvict(value = {"postList"}, allEntries = true)
    @CacheEvict(value = {"post", "postList"}, allEntries = true)
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
        if (postDTO.getCategories() != null) {
            for (CategoryDTO c : postDTO.getCategories()) {
                Category category = categoryServ.findOrCreate(c.getTitle(), authorId);
                // 检查分类是否已禁用
                if (category.getStatus() == null || category.getStatus() != 1) {
                    throw new BizException("分类 [" + category.getTitle() + "] 已被禁用，无法使用");
                }
                log.info("准备插入文章分类映射：postId={}, categoryId={}", postId, category.getId());
                postCategoryMapper.insertIfNotExist(postId, category.getId());
                log.info(MessageConstants.POST_CATEGORY_INSERT_SUCCESS + ": 绑定分类 [{}] -> 文章 [{}]", category.getTitle(), post.getTitle());
            }
        }

        // 3️⃣ 标签处理
        if (postDTO.getLabels() != null) {
            for (LabelDTO l : postDTO.getLabels()) {
                Label label = labelServ.findOrCreate(l.getTitle(), authorId);
                // 检查标签是否已禁用
                if (label.getStatus() == null || label.getStatus() != 1) {
                    throw new BizException("标签 [" + label.getTitle() + "] 已被禁用，无法使用");
                }
                log.info("准备插入文章标签映射：postId={}, labelId={}", postId, label.getId());
                postLabelMapper.insertIfNotExist(postId, label.getId());
                log.info(MessageConstants.POST_LABEL_INSERT_SUCCESS + ": 绑定标签 [{}] -> 文章 [{}]", label.getTitle(), post.getTitle());
            }
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
    @Caching(evict = {
        @CacheEvict(value = "post", key = "'id:' + #postId"),
        @CacheEvict(value = "postList", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postUpdate(BigInteger postId, PostDTO postDTO) {
        // 获取用户Id
        BigInteger authorId = getCurrentUserId();

        // 1️⃣ 更新主表
//        Post post = new Post();
        Post post = postMapper.selectById(postId);
        
        // 检查用户权限
        boolean isAdmin = isAdminUser();
        
        // 非所有者且非管理员不可修改
        if (!Objects.equals(post.getAuthorId(), authorId) && !isAdmin) {
            throw new BizException(MessageConstants.POST_UPDATE_FAILED + ": " + post.getTitle());
        }
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

        // 读取更新后的文章
        Post updated = postMapper.selectById(postId);
        if (updated != null) {
            postAssembler.enrichPost(updated);
        }

        // 2️⃣ 重建分类关联
        if (postDTO.getCategories() != null && !postDTO.getCategories().isEmpty()) {
            postCategoryMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS);
            for (CategoryDTO c : postDTO.getCategories()) {
                Category category = categoryServ.findOrCreate(c.getTitle(), authorId);
                // 检查分类是否已禁用
                if (category.getStatus() == null || category.getStatus() != 1) {
                    throw new BizException("分类 [" + category.getTitle() + "] 已被禁用，无法使用");
                }
                postCategoryMapper.insertIfNotExist(postId, category.getId());
                log.info(MessageConstants.POST_CATEGORY_INSERT_SUCCESS + ": 绑定分类 [{}] -> 文章 [{}]", category.getTitle(), post.getTitle());
            }
        }

        // 3️⃣ 重建标签关联
        if (postDTO.getLabels() != null && !postDTO.getLabels().isEmpty()) {
            postLabelMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS);
            for (LabelDTO l : postDTO.getLabels()) {
                Label label = labelServ.findOrCreate(l.getTitle(), authorId);
                // 检查标签是否已禁用
                if (label.getStatus() == null || label.getStatus() != 1) {
                    throw new BizException("标签 [" + label.getTitle() + "] 已被禁用，无法使用");
                }
                postLabelMapper.insertIfNotExist(postId, label.getId());
                log.info(MessageConstants.POST_LABEL_INSERT_SUCCESS + ": 绑定标签 [{}] -> 文章 [{}]", label.getTitle(), post.getTitle());
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
    @Caching(evict = {
        @CacheEvict(value = "post", key = "'id:' + #postId"),
        @CacheEvict(value = "postList", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postDeleteById(BigInteger postId) {
        // 每次调用方法时动态获取
        BigInteger authorId = getCurrentUserId();
        // 读取文章标题以便针对性驱逐 postsByTitle
        Post toDeletePost = postMapper.selectById(postId);
        // 非所有者不可删除
        if (!Objects.equals(toDeletePost.getAuthorId(), authorId)) {
            throw new BizException(MessageConstants.POST_DELETE_FAILED + ": " + toDeletePost.getTitle());
        }

        // Use helper to perform deletion and mapping cleanup
        deletePostAndMappings(postId, authorId, toDeletePost);

        return Boolean.TRUE;
    }

    /**
     * 根据分类删除文章
     * @param categoryId 分类
     * @return 删除结果
     */
    @CacheEvict(value = {"post", "postList"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postDeleteByCategory(BigInteger categoryId) {
        // 1. 查询包含该分类Id的文章
//        List<BigInteger> postIds = postCategoryMapper.selectByCategoryId(categoryId);

        // 代理查询
        List<BigInteger> postIds = postServProxy.postIdsByCategory(categoryId);
        BigInteger operatorId = getCurrentUserId();
        // 使用批量删除以减少数据库往返
        deletePostsBatch(postIds, operatorId);

        return Boolean.TRUE;
    }

    /**
     * 根据用户Id删除文章
     *
     * @param userId 用户Id
     * @return 删除结果
     */
    @CacheEvict(value = {"post", "postList"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Boolean postDeleteByUserId(BigInteger userId) {
        List<BigInteger> posts = postMapper.selectByAuthorId(userId);
        if (posts == null || posts.isEmpty()) return Boolean.TRUE;
        List<BigInteger> postIds = new ArrayList<>();
        for (BigInteger p : posts) postIds.add(p);

        BigInteger operatorId = getCurrentUserId();
        deletePostsBatch(postIds, operatorId);

        return Boolean.TRUE;
    }

    @CacheEvict(value = {"post", "postList"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean postDeleteByLabel(BigInteger labelId) {
        // 1. 查询包含该分类Id的文章
//        List<BigInteger> postIds = postLabelMapper.selectBylabelId(labelId);
        // 代理查询
        List<BigInteger> postIds = postServProxy.postIdsByLabel(labelId);
        BigInteger operatorId = getCurrentUserId();
        deletePostsBatch(postIds, operatorId);

        return Boolean.TRUE;
    }

    /**
     * 批量删除文章（先删除映射，再批量删除文章）
     * @param postIds 文章ID集合
     * @param operatorId 操作者ID（用于日志）
     */

    private void deletePostsBatch(List<BigInteger> postIds, BigInteger operatorId) {
        if (postIds == null || postIds.isEmpty()) {
            log.debug("没有需要删除的文章");
            return;
        }

        // 先删除映射表中的关系（批量）
            postLabelMapper.deleteByPostIds(postIds);
            log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS + ": posts {}", postIds);
            postCategoryMapper.deleteByPostIds(postIds);
            log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS + ": posts {}", postIds);

        // 批量删除主表文章
        int deleted = postMapper.deleteByIds(postIds);
        log.info(MessageConstants.POST_DELETE_SUCCESS + ": 删除文章数量({}) (操作者:{})", deleted, operatorId);
    }
    /**
     * 公共删除逻辑：删除文章并清理关联映射、记录日志
     *
     * @param postId       文章ID
     * @param operatorId   执行删除的用户ID（用于日志）
     * @param maybePostObj 可能已经加载的 Post 对象（可为 null）
     */
    private void deletePostAndMappings(BigInteger postId, BigInteger operatorId, Post maybePostObj) {
        Post toDelete = maybePostObj == null ? postMapper.selectById(postId) : maybePostObj;

        // 使用 toDelete 避免未使用变量警告，并提供更有用的调试信息
        if (toDelete != null) {
            log.debug("准备删除文章 -> id: {}, title: {}", postId, toDelete.getTitle());
        }

        if (postMapper.deleteById(postId) == 1) {
            log.info(MessageConstants.POST_DELETE_SUCCESS + ": 文章ID({}) (操作者:{})", postId, operatorId);
            // 删除对应的文章-标签表/分类表
            postLabelMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_LABEL_DELETE_SUCCESS + ": 文章ID({})", postId);
            postCategoryMapper.deleteByPostId(postId);
            log.info(MessageConstants.POST_CATEGORY_DELETE_SUCCESS + ": 文章ID({})", postId);
        } else {
            log.warn(MessageConstants.POST_DELETE_FAILED + ": 文章(ID:{})", postId);
        }
    }

    /*--------------------查询部分------------------------------------------------------*/

    /**
     * 查询所有文章
     * @return 文章列表
     */
    @Cacheable(value = "postList", key = "'all'", unless = "#result == null or #result.isEmpty()")
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
        // redis中不存在则从数据库中读取并初始化redis
        if (!redisUtils.hasKey(MessageConstants.REDIS_POST_LIKES)) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                redisUtils.increZSetScore(MessageConstants.REDIS_POST_LIKES, postId.toString(), Long.valueOf(post.getLikes()));
            }
        }
        // 写入 Redis 中的有序集合以记录增量（不立即写库）
        redisUtils.increZSetScore(MessageConstants.REDIS_POST_LIKES, postId.toString(), 1L);
        log.info(MessageConstants.POST_LIKE_SUCCESS + " (ID:{})", postId);
        return Boolean.TRUE;
    }

    @Override
    public Boolean postIncreViews(BigInteger postId) {
//        postMapper.incrementViews(postId);
//        redisUtils.increZSetScore(MessageConstants.REDIS_POST_VIEWS,postId.toString(),1L);
        // redis中不存在则从数据库中读取并初始化redis
        if (!redisUtils.hasKey(MessageConstants.REDIS_POST_VIEWS)) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                redisUtils.increZSetScore(MessageConstants.REDIS_POST_VIEWS, postId.toString(), Long.valueOf(post.getViews()));
            }
        }
        // 写入 Redis 有序集合记录浏览量增量（不立即写库）
        redisUtils.increZSetScore(MessageConstants.REDIS_POST_VIEWS, postId.toString(), 1L);
        log.info(MessageConstants.POST_VIEW_SUCCESS + " (ID:{})", postId);
        return Boolean.TRUE;
    }

    /**
     * 根据id查找文章
     * @param postId 文章id
     * @return 查找结果
     */
    @Cacheable(value = "post", key = "'id:' + #postId", unless = "#result == null")
    @Override
    public Post postSelectById(BigInteger postId) {
        // 设置文章其他信息
        Post post = postMapper.selectById(postId);
        if (post == null)
            return null;
        postAssembler.enrichPost(post);
        log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postId);
        return post;
    }

    /* ************************* 优化点：按分类/标签查询改为缓存文章ID列表 *************************
     * 说明：将分类/标签/标题到文章ID的映射缓存（轻量），并复用按ID缓存（postsById）来加载每篇文章，
     * 避免把同一篇文章对象在多个集合缓存中重复存储，降低缓存失效的范围及存储成本。
     */

    /**
     * 根据标题查询文章ID（缓存轻量的ID列表）
     * @param title 文章标题
     * @return 文章ID列表
     */
    @Cacheable(value = "postList", key = "'title:' + #title", unless = "#result == null or #result.isEmpty()")
    public List<BigInteger> postIdsByTitle(String title) {
        return postMapper.selectByTitle(title);
    }
    /**
     * 根据标题查找文章
     * @param title 文章名
     * @return 查找结果
     */
    @Override
    public List<Post> postSelectByTitle(String title) {
//        List<BigInteger> posts = postMapper.selectByTitle(title);
//        List<Post> posts = postMapper.selectByIds(posts);
//        postAssembler.enrichPosts(posts);
//        log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章名: {}", title);

        // 通过代理调用带缓存的方法以保证缓存生效
        List<BigInteger> postIds = postServProxy.postIdsByTitle(title);
        log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postIds);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postServProxy.postSelectById(postId); // 利用 postsById 缓存（通过代理触发）
            if (post != null) {
                posts.add(post);
                log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postId);
            }
        }

        return posts;
    }

    /**
     * 根据作者查询文章ID（缓存轻量的ID列表）
     * @param authorId 作者Id
     * @return 文章ID列表
     */
    @Cacheable(value = "postList", key = "'authorId:' + #authorId", unless = "#result == null or #result.isEmpty()")
    public List<BigInteger> postIdsByAuthor(BigInteger authorId) {
        return postMapper.selectByAuthorId(authorId);
    }

    /**
     * 根据作者查询文章
     *
     * @param authorId 作者Id
     * @return 查询文章的集合
     */
    @Override
    public List<Post> postSelectByAuthor(BigInteger authorId) {
        // 通过代理调用带缓存的方法以保证缓存生效
        List<BigInteger> postIds = postServProxy.postIdsByAuthor(authorId);
        log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postIds);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postServProxy.postSelectById(postId); // 利用 postsById 缓存（通过代理触发）
            if (post != null) {
                posts.add(post);
                log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postId);
            }
        }
        return posts;
    }

    /**
     * 根据分类查询文章ID（缓存轻量的ID列表）
     * @param categoryId 分类Id
     * @return 文章ID列表
     */
    @Cacheable(value = "postList", key = "'category:' + #categoryId", unless = "#result == null or #result.isEmpty()")
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
        log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postIds);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postServProxy.postSelectById(postId); // 利用 postsById 缓存（通过代理触发）
            if (post != null) {
                posts.add(post);
                log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postId);
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
    @Cacheable(value = "postList", key = "'label:' + #labelId", unless = "#result == null or #result.isEmpty()")
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
        log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postIds);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postServProxy.postSelectById(postId); // 利用 postsById 缓存（通过代理触发）
            if (post != null) {
                posts.add(post);
                log.info(MessageConstants.POST_SELECT_SUCCESS + ": 文章ID: {}", postId);
            }
        }
        return posts;
    }
}
