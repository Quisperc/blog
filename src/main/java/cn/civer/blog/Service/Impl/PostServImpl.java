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
    public Result postAdd(PostDTO postDTO) {
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
        log.info("成功插入文章：[{}] (ID: {})", post.getTitle(), postId);

        // 2️⃣ 分类处理
        for (CategoryDTO c : postDTO.getCategories()) {
            Category category = categoryServ.findOrCreate(c.getTitle(), c.getSummary(), authorId);
            log.info("准备插入文章分类映射：postId={}, categoryId={}", postId, category.getId());
            postCategoryMapper.insertIfNotExist(postId, category.getId());
            log.info("绑定分类 [{}] -> 文章 [{}]", category.getTitle(), post.getTitle());
        }

        // 3️⃣ 标签处理
        for (LabelDTO l : postDTO.getLabels()) {
            Label label = labelServ.findOrCreate(l.getTitle(), l.getSummary(), authorId);
            log.info("准备插入文章标签映射：postId={}, labelId={}", postId, label.getId());
            postLabelMapper.insertIfNotExist(postId, label.getId());
            log.info("绑定标签 [{}] -> 文章 [{}]", label.getTitle(), post.getTitle());
        }
        return Result.success(MessageConstants.POST_ADD_SUCCESS);
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
    public Result postUpdate(BigInteger postId, PostDTO postDTO) {
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
            for (CategoryDTO c : postDTO.getCategories()) {
                Category category = categoryServ.findOrCreate(c.getTitle(), c.getSummary(), authorId);
                postCategoryMapper.insertIfNotExist(postId, category.getId());
            }
        }

        // 3️⃣ 重建标签关联
        if (!postDTO.getLabels().isEmpty()) {
            postLabelMapper.deleteByPostId(postId);
            for (LabelDTO l : postDTO.getLabels()) {
                Label label = labelServ.findOrCreate(l.getTitle(), l.getSummary(), authorId);
                postLabelMapper.insertIfNotExist(postId, label.getId());
            }
        }
        log.info(MessageConstants.POST_UPDATE_SUCCESS + " (ID:{}) ", postId);
        return Result.success(MessageConstants.POST_UPDATE_SUCCESS);
    }

    /**
     * 根据文章ID删除文章
     *
     * @param id 文章Id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result postDeleteById(BigInteger id) {
        // 每次调用方法时动态获取
        BigInteger authorId = getCurrentUserId();
        // 1. 文章删除成功
        if (postMapper.deleteById(id) == 1) {
            log.info("文章(ID:{})已被 (用户:{}) 删除", id, authorId);
            // 2. 删除对应的文章-标签表/分类表
            postLabelMapper.deleteByPostId(id);
            postCategoryMapper.deleteByPostId(id);
            return Result.success(MessageConstants.POST_DELETE_SUCCESS);
        }
        return Result.error(MessageConstants.POST_DELETE_FAILED);
    }

    /**
     * 根据分类删除文章
     *
     * @param categoryId 分类
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result postDeleteByCategory(BigInteger categoryId) {
        // 1. 查询包含该分类Id的文章
        List<BigInteger> postIds = postCategoryMapper.selectByCategoryId(categoryId);
        // 2. 遍历postIds
        for (BigInteger postId : postIds) {
            // 3. 根据Id删除文章
            if (postMapper.deleteById(postId) == 1) {
                log.info("删除文章(" + postId + ")成功");
            }
            // 4. 根据Id删除文章-分类表
            if (postCategoryMapper.deleteByPostId(postId) == 1) {
                log.info("删除文章(" + postId + ")-分类表成功");
            }
            // 5. 根据Id删除文章-标签表
            if (postLabelMapper.deleteByPostId(postId) == 1) {
                log.info("删除文章(" + postId + ")-标签表成功");
            }
        }
        return Result.success("删除文章成功");
    }

    /**
     * 根据用户Id删除文章
     *
     * @param userId 用户Id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result postDeleteByUserId(BigInteger userId) {
        List<Post> posts = postMapper.selectByAuthorId(userId);
        if (posts.isEmpty())
            return Result.error("无文章");
        for (Post post : posts) {
            // 1. 文章删除成功
            if (postMapper.deleteById(post.getId()) == 1) {
                log.info("文章(ID:" + post.getId() + ")已被删除");
                // 2. 删除对应的文章-标签表/分类表
                postLabelMapper.deleteByPostId(post.getId());
                postCategoryMapper.deleteByPostId(post.getId());
            } else {
                // 文章删除失败
                log.warn("文章(ID:" + post.getId() + ")无法被删除");
            }
        }
        return Result.success();
    }

    /**
     * 根据标签删除文章
     *
     * @param labelId 标签
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result postDeleteByLabel(BigInteger labelId) {
        // 1. 查询包含该分类Id的文章
        List<BigInteger> postIds = postLabelMapper.selectBylabelId(labelId);
        // 2. 遍历postIds
        for (BigInteger postId : postIds) {
            // 3. 根据Id删除文章
            if (postMapper.deleteById(postId) == 1) {
                log.info("删除文章(" + postId + ")成功");
            }
            // 4. 根据Id删除文章-分类表
            if (postCategoryMapper.deleteByPostId(postId) == 1) {
                log.info("删除文章(" + postId + ")-分类表成功");
            }
            // 5. 根据Id删除文章-标签表
            if (postLabelMapper.deleteByPostId(postId) == 1) {
                log.info("删除文章(" + postId + ")-标签表成功");
            }
        }
        return Result.success("删除文章成功");
    }

    /*--------------------查询部分------------------------------------------------------*/

    /**
     * 查询所有文章
     *
     * @return 文章列表
     */
    @Override
    public Result postSelectAll() {
        List<Post> posts = postMapper.selectAll();
        if (posts.isEmpty())
            return Result.success("无文章");
        postAssembler.enrichPosts(posts);
        return Result.success(posts);
    }

    /**
     * 根据id查找文章
     *
     * @param id 文章id
     * @return 查找结果
     */
    @Override
    public Result postSelectById(BigInteger id) {
        // 设置文章其他信息
        Post post = postMapper.selectById(id);
        if (post == null)
            return Result.error("文章不存在");
        postAssembler.enrichPost(post);
        return Result.success(post);
    }

    /**
     * 根据标题查找文章
     *
     * @param title 文章名
     * @return 查找结果
     */
    @Override
    public Result postSelectByTitle(String title) {
        List<Post> posts = postMapper.selectByTitle(title);
        postAssembler.enrichPosts(posts);
        return Result.success(posts);
    }

    /**
     * 根据分类查询文章
     *
     * @param categoryId 分类Id
     * @return 查询文章的集合
     */
    @Override
    public Result postSelectByCategory(BigInteger categoryId) {
        List<BigInteger> postIds = postCategoryMapper.selectByCategoryId(categoryId);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postMapper.selectById(postId);
            postAssembler.enrichPost(post);
            posts.add(post);
            log.info("查找文章(" + post.getId() + ")成功");
        }
        return Result.success(posts);
    }

    /**
     * 根据标签查询文章
     *
     * @param labelId 标签
     * @return 查询文章的集合
     */
    @Override
    public Result postSelectByLabel(BigInteger labelId) {
        List<BigInteger> postIds = postLabelMapper.selectBylabelId(labelId);
        List<Post> posts = new ArrayList<>();
        for (BigInteger postId : postIds) {
            Post post = postMapper.selectById(postId);
            postAssembler.enrichPost(post);
            posts.add(post);
            log.info("查找文章(" + post.getId() + ")成功");
        }
        return Result.success(posts);
    }
}
