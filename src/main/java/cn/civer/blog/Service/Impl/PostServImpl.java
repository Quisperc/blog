package cn.civer.blog.Service.Impl;

import cn.civer.blog.Mapper.*;
import cn.civer.blog.Model.DTO.PostCategoryDTO;
import cn.civer.blog.Model.DTO.PostLabelDTO;
import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.Post;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.PostServ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PostServImpl implements PostServ {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private PostLabelMapper postLabelMapper;
    @Autowired
    private PostCategoryMapper postCategoryMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private LabelMapper labelMapper;

    /**
     * 新增文章
     * @param post 文章
     * @return 操作结果
     */
    @Override
    public Result postAdd(Post post) {
        try {
            // 每次调用方法时动态获取
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                return Result.error( "未登录或身份无效！");
            }
            // 1. 文章表
            // 1.1 设置作者
            post.setAuthorId(new BigInteger(authentication.getName()));
            // 1.2 插入文章表并获取文章ID
            postMapper.insert(post);
            BigInteger postId = post.getId();
            log.info("成功插入文章："+post.getTitle()+"，文章ID："+postId);

            // 2. 分类表以及文章分类表
            // 2.1 获取分类
            List<Category> postCategory = post.getCategorys();
            // 2.2 遍历分类
            for(Category cate: postCategory){
                // 2.3 验证分类是否存在,不存在则创建
                Category validCate = categoryMapper.selectByTitle(cate.getTitle());
                if(validCate == null) {
                    cate.setAuthorId(new BigInteger(authentication.getName()));
                    categoryMapper.insert(cate);
                    log.info("成功创建分类："+cate.getTitle());
                    // 2.4 获取文章-分类DTO
                    PostCategoryDTO postCategoryDTO = new PostCategoryDTO();
                    postCategoryDTO.setPostId(postId);
                    postCategoryDTO.setCategoryId(cate.getId());
                    // 2.5 校验文章-标签该记录是否已存在
                    List<BigInteger> postCategorysIds =  postCategoryMapper.selectByPostAndCategory(postCategoryDTO);
                    if(postCategorysIds.isEmpty()) {
                        // 2.6 将文章-标签插入文章-标签表
                        postCategoryMapper.insert(postCategoryDTO);
                        log.info("成功插入文章-分类表：" + postId + "--" + cate.getId());
                    }else{
                        log.warn("插入文章-分类表：" + postId + "--" + cate.getId()+"失败，文章已记录！");
                    }
                }else{
                    // 2.4 获取文章-分类DTO
                    PostCategoryDTO postCategoryDTO = new PostCategoryDTO();
                    postCategoryDTO.setPostId(postId);
                    postCategoryDTO.setCategoryId(validCate.getId());
                    // 2.5 校验文章-标签该记录是否已存在
                    List<BigInteger> postCategorysIds =  postCategoryMapper.selectByPostAndCategory(postCategoryDTO);
                    if(postCategorysIds.isEmpty()) {
                        // 2.6 将文章-标签插入文章-标签表
                        postCategoryMapper.insert(postCategoryDTO);
                        log.info("成功插入文章-分类表：" + postId + "--" + validCate.getId());
                    }else{
                        log.warn("插入文章-分类表：" + postId + "--" + validCate.getId()+"失败，文章已记录！");
                    }
                }
            }

            // 3 标签表以及文章-标签表
            // 3.1 获取标签
            List<Label> postLabels = post.getLabels();
            // 3.2 遍历标签
            for(Label label: postLabels){
                // 3.3 验证标签是否存在,不存在则创建
                Label validLabel = labelMapper.selectByTitle(label.getTitle());
                if(validLabel == null) {
                    label.setAuthorId(new BigInteger(authentication.getName()));
                    labelMapper.insert(label);
                    log.info("成功创建标签："+label.getTitle());
                    // 3，4 获取文章-标签DTO
                    PostLabelDTO postLabelDTO = new PostLabelDTO();
                    postLabelDTO.setLabelId(label.getId());
                    postLabelDTO.setPostId(postId);
                    // 3.5 校验文章-标签该记录是否已存在
                    List<BigInteger> postLabelsIds =  postLabelMapper.selectByPostAndlabel(postLabelDTO);
                    if(postLabelsIds.isEmpty()) {
                        // 3.6 将文章-标签插入文章-标签表
                        postLabelMapper.insert(postLabelDTO);
                        log.info("成功插入文章-标签表：" + postId + "--" + label.getId());
                    }else{
                        log.warn("插入文章-标签表：" + postId + "--" + label.getId()+"失败，文章已记录！");
                    }
                }else{
                    // 3，4 获取文章-标签DTO
                    PostLabelDTO postLabelDTO = new PostLabelDTO();
                    postLabelDTO.setLabelId(validLabel.getId());
                    postLabelDTO.setPostId(postId);
                    // 3.5 校验文章-标签该记录是否已存在
                    List<BigInteger> postLabelsIds =  postLabelMapper.selectByPostAndlabel(postLabelDTO);
                    if(postLabelsIds.isEmpty()) {
                        // 3.6 将文章-标签插入文章-标签表
                        postLabelMapper.insert(postLabelDTO);
                        log.info("成功插入文章-标签表：" + postId + "--" + validLabel.getId());
                    }else{
                        log.warn("插入文章-标签表：" + postId + "--" + validLabel.getId()+"失败，文章已记录！");
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.error("插入文章："+post.getTitle()+"，文章ID："+post.getId()+"失败："+e);
            return Result.error("文章保存失败！");
        }
        return Result.success("文章保存成功！");
    }

    /**
     * 根据文章ID删除文章
     * @param id 文章Id
     * @return 删除结果
     */
    @Override
    public Result postDeleteById(BigInteger id) {
        // 每次调用方法时动态获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            // 1. 文章删除成功
            if(postMapper.deleteById(id) == 1){
                log.info("文章(ID:"+id+")已被"+authentication.getName()+"删除");
                // 2. 删除对应的文章-标签表/分类表
                postLabelMapper.deleteByPostId(id);
                postCategoryMapper.deleteByPostId(id);
            }else {
                // 文章删除失败
                log.warn("文章(ID:"+id+")无法被"+authentication.getName()+"删除");
            }
            return Result.success("文章已删除");
        } catch (Exception e) {
            log.error("文章(ID:"+id+")无法被"+authentication.getName()+"删除，文章不存在/用户权限不足："+e);
            return Result.error("文章删除失败");
        }
    }

    /**
     * 根据Title删除文章
     * @param title 文章名
     * @return 删除结果
     */
    @Override
    public Result postDeleteByTitle(String title) {
        // 每次调用方法时动态获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            // 1. 获取文章
            List<Post> posts =  postMapper.selectByTitle(title);
            // 2. 遍历获取的文章
            for(Post post:posts){
                // 3. 文章删除成功
                if(postMapper.deleteById(post.getId()) == 1){
                    log.info("文章(ID:"+post.getId()+")已被"+authentication.getName()+"删除");
                    // 4. 删除对应的文章-标签表/分类表
                    postLabelMapper.deleteByPostId(post.getId());
                    postCategoryMapper.deleteByPostId(post.getId());
                }else {
                    // 文章删除失败
                    log.warn("文章(ID:"+post.getId()+")无法被"+authentication.getName()+"删除");
                }
            }
            return Result.success("文章已删除");
        } catch (Exception e) {
            log.error("文章()无法被"+authentication.getName()+"删除，文章不存在/用户权限不足："+e);
            return Result.error("文章删除失败");
        }
    }

    /**
     * 根据分类删除文章
     * @param category 分类
     * @return 删除结果
     */
    @Override
    public Result postDeleteByCategory(Category category) {
        try {
            // 1. 查询包含该分类Id的文章
            List<BigInteger> postIds = postCategoryMapper.selectByCategoryId(category.getId());
            // 2. 遍历postIds
            for(BigInteger postId:postIds){
                // 3. 根据Id删除文章
                if(postMapper.deleteById(postId) == 1){
                    log.info("删除文章("+postId+")成功");
                }
                // 4. 根据Id删除文章-分类表
                if(postCategoryMapper.deleteByPostId(postId) == 1){
                    log.info("删除文章("+postId+")-分类表成功");
                }
                // 5. 根据Id删除文章-标签表
                if(postLabelMapper.deleteByPostId(postId) == 1){
                    log.info("删除文章("+postId+")-标签表成功");
                }
            }
            return Result.success("删除文章成功");
        } catch (Exception e) {
            log.error("删除文章失败："+e);
            return Result.error("删除文章失败");
        }
    }

    /**
     * 根据标签删除文章
     * @param label 标签
     * @return 删除结果
     */
    @Override
    public Result postDeleteByLabel(Label label) {
        try {
            // 1. 查询包含该分类Id的文章
            List<BigInteger> postIds = postLabelMapper.selectBylabelId(label.getId());
            // 2. 遍历postIds
            for(BigInteger postId:postIds){
                // 3. 根据Id删除文章
                if(postMapper.deleteById(postId) == 1){
                    log.info("删除文章("+postId+")成功");
                }
                // 4. 根据Id删除文章-分类表
                if(postCategoryMapper.deleteByPostId(postId) == 1){
                    log.info("删除文章("+postId+")-分类表成功");
                }
                // 5. 根据Id删除文章-标签表
                if(postLabelMapper.deleteByPostId(postId) == 1){
                    log.info("删除文章("+postId+")-标签表成功");
                }
            }
            return Result.success("删除文章成功");
        } catch (Exception e) {
            log.error("删除文章失败："+e);
            return Result.error("删除文章失败");
        }
    }

    /**
     * 更新文章
     * @param post 文章
     * @return 修改结果
     */
    @Override
    public Result postUpdate(Post post) {
        try {
            // 1. 更新文章
            postMapper.update(post);
            // 2. 获取文章标签与分类
            List<Label> labels = post.getLabels();
            List<Category> categories = post.getCategorys();
            if(!labels.isEmpty()) {
                // 2.1. 删除原标签
                postLabelMapper.deleteByPostId(post.getId());
                // 2.2 添加新标签
                for (Label label : labels) {
                    PostLabelDTO postLabelDTO = new PostLabelDTO();
                    postLabelDTO.setPostId(post.getId());
                    postLabelDTO.setLabelId(label.getId());
                    postLabelMapper.insert(postLabelDTO);
                }
            }
            if(!categories.isEmpty()) {
                // 3.1 删除原分类
                postCategoryMapper.deleteByPostId(post.getId());
                // 3.2 添加新分类
                for (Category category : categories) {
                    PostCategoryDTO postCategoryDTO = new PostCategoryDTO();
                    postCategoryDTO.setPostId(post.getId());
                    postCategoryDTO.setCategoryId(category.getId());
                    postCategoryMapper.insert(postCategoryDTO);
                }
            }
            log.info("修改文章成功");
            return Result.success("修改文章成功");
        } catch (Exception e) {
            log.error("修改文章失败："+e);
            return Result.error("修改文章失败");
        }
    }

    /**
     * 根据id查找文章
     * @param id 文章id
     * @return 查找结果
     */
    @Override
    public Result postSelectById(BigInteger id) {
        try {
            Post post =  postMapper.selectById(id);
            List categories = postCategoryMapper.selectByPostId(id);
            List labels = postLabelMapper.selectByPostId(id);
            post.setCategorys(categories);
            post.setLabels(labels);
            log.info("查找文章("+id+")成功");
            return Result.success(post);
        } catch (Exception e) {
            log.error("查找文章失败："+e);
            return Result.error("查找文章失败");
        }
    }

    /**
     * 根据标题查找文章
     * @param title 文章名
     * @return 查找结果
     */
    @Override
    public Result postSelectByTitle(String title) {
        try {
            List<Post> posts = postMapper.selectByTitle(title);
            for(Post post:posts) {
                List categories = postCategoryMapper.selectByPostId(post.getId());
                List labels = postLabelMapper.selectByPostId(post.getId());
                post.setCategorys(categories);
                post.setLabels(labels);
                log.info("查找文章(" + post.getId() + ")成功");
            }
            return Result.success(posts);
        } catch (Exception e) {
            log.error("查找文章失败："+e);
            return Result.error("查找文章失败");
        }
    }

    /**
     * 根据分类查询文章
     * @param category 分类
     * @return 查询文章的集合
     */
    @Override
    public Result postSelectByCategory(Category category) {
        try {
            List<BigInteger> postIds = postCategoryMapper.selectByCategoryId(category.getId());
            List<Post> posts = new ArrayList<>();
            for(BigInteger postId:postIds) {
                Post post = postMapper.selectById(postId);
                List categories = postCategoryMapper.selectByPostId(postId);
                List labels = postLabelMapper.selectByPostId(postId);
                post.setCategorys(categories);
                post.setLabels(labels);
                posts.add(post);
                log.info("查找文章(" + post.getId() + ")成功");
            }
            return Result.success(posts);
        } catch (Exception e) {
            log.error("查找文章失败："+e);
            return Result.error("查找文章失败");
        }
    }

    /**
     * 根据标签查询文章
     * @param label 标签
     * @return 查询文章的集合
     */
    @Override
    public Result postSelectByLabel(Label label) {
        try {
            List<BigInteger> postIds = postLabelMapper.selectBylabelId(label.getId());
            List<Post> posts = new ArrayList<>();
            for(BigInteger postId:postIds) {
                Post post = postMapper.selectById(postId);
                List categories = postCategoryMapper.selectByPostId(postId);
                List labels = postLabelMapper.selectByPostId(postId);
                post.setCategorys(categories);
                post.setLabels(labels);
                posts.add(post);
                log.info("查找文章(" + post.getId() + ")成功");
            }
            return Result.success(posts);
        } catch (Exception e) {
            log.error("查找文章失败："+e);
            return Result.error("查找文章失败");
        }
    }
}
