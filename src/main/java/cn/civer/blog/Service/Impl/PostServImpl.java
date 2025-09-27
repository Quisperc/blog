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
    @Override
    public Result postAdd(Post post) {
        try {
            // 每次调用方法时动态获取
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                return Result.error("操作失败", "未登录或身份无效！");
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
                    // postCategoryMapper.insert(postId,cate.getId());
                    PostCategoryDTO postCategoryDTO = new PostCategoryDTO();
                    postCategoryDTO.setPostId(postId);
                    postCategoryDTO.setCategoryId(cate.getId());
                    // 2.5 将文章-分类插入文章分类表
                    // postCategoryMapper.insert(postCategoryDTO);
                    // log.info("成功插入文章-分类表："+postId+"--"+cate.getId());
                    // 2.5 校验文章-标签该记录是否已存在
                    List<BigInteger> postCategorysIds =  postCategoryMapper.selectByPostAndCategory(postCategoryDTO);
                    if(postCategorysIds.isEmpty()) {
                        // 2.6 将文章-标签插入文章-标签表
                        postCategoryMapper.insert(postCategoryDTO);
                        log.info("成功插入文章-分类表：" + postId + "--" + cate.getId());
                    }else{
                        log.info("插入文章-分类表：" + postId + "--" + cate.getId()+"失败，文章已记录！");
                    }
                }else{
                    // 2.4 将文章-分类插入文章分类表
                    // postCategoryMapper.insert(postId,validCate.getId());
                    // 2.4 获取文章-分类DTO
                    // postCategoryMapper.insert(postId,cate.getId());
                    PostCategoryDTO postCategoryDTO = new PostCategoryDTO();
                    postCategoryDTO.setPostId(postId);
                    postCategoryDTO.setCategoryId(validCate.getId());

                    // 2.5 将文章-分类插入文章分类表
                    // postCategoryMapper.insert(postCategoryDTO);
                    // log.info("成功插入文章-分类表："+postId+"--"+validCate.getId());
                    // 2.5 校验文章-标签该记录是否已存在
                    List<BigInteger> postCategorysIds =  postCategoryMapper.selectByPostAndCategory(postCategoryDTO);
                    if(postCategorysIds.isEmpty()) {
                        // 2.6 将文章-标签插入文章-标签表
                        postCategoryMapper.insert(postCategoryDTO);
                        log.info("成功插入文章-分类表：" + postId + "--" + validCate.getId());
                    }else{
                        log.info("插入文章-分类表：" + postId + "--" + validCate.getId()+"失败，文章已记录！");
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
                    // 3.5 将文章-标签插入文章-标签表
                    // postLabelMapper.insert(postId,label.getId());
                    // postLabelMapper.insert(postLabelDTO);
                    // log.info("成功插入文章-标签表："+postId+"--"+label.getId());
                    // 3.5 校验文章-标签该记录是否已存在
                    List<BigInteger> postLabelsIds =  postLabelMapper.selectByPostAndlabel(postLabelDTO);
                    if(postLabelsIds.isEmpty()) {
                        // 3.6 将文章-标签插入文章-标签表
                        postLabelMapper.insert(postLabelDTO);
                        log.info("成功插入文章-标签表：" + postId + "--" + label.getId());
                    }else{
                        log.info("插入文章-标签表：" + postId + "--" + label.getId()+"失败，文章已记录！");
                    }
                }else{
                    // 3.4 将文章-标签插入文章-标签表
                    // postLabelMapper.insert(postId,validLabel.getId());
                    // log.info("成功插入文章-标签表："+postId+"--"+validLabel.getId());
                    // 3，4 获取文章-标签DTO
                    PostLabelDTO postLabelDTO = new PostLabelDTO();
                    postLabelDTO.setLabelId(validLabel.getId());
                    postLabelDTO.setPostId(postId);
                    // postLabelMapper.insert(postId,label.getId());
                    // 3.5 校验文章-标签该记录是否已存在
                    List<BigInteger> postLabelsIds =  postLabelMapper.selectByPostAndlabel(postLabelDTO);
                    if(postLabelsIds.isEmpty()) {
                        // 3.6 将文章-标签插入文章-标签表
                        postLabelMapper.insert(postLabelDTO);
                        log.info("成功插入文章-标签表：" + postId + "--" + validLabel.getId());
                    }else{
                        log.info("插入文章-标签表：" + postId + "--" + validLabel.getId()+"失败，文章已记录！");
                    }
                }
            }
        } catch (NumberFormatException e) {
            return Result.error("操作失败","文章保存失败！");
        }
        return Result.Success("操作成功","文章保存成功！");
    }

    @Override
    public Result postDeleteById(BigInteger id) {
        postMapper.deleteByIdInt(id);
        return null;
    }

    @Override
    public Result postDeleteByTitle(String title) {
        return null;
    }

    @Override
    public Result postDeleteByCategory(Category category) {
        return null;
    }

    @Override
    public Result postDeleteByLabel(Label label) {
        return null;
    }

    @Override
    public Result postUpdate(Post post) {
        return null;
    }

    @Override
    public Result postSelectById(BigInteger id) {
        return null;
    }

    @Override
    public Result postSelectByTitle(String title) {
        return null;
    }

    @Override
    public Result postSelectByCategory(Category category) {
        return null;
    }

    @Override
    public Result postSelectByLabel(Label label) {
        return null;
    }
}
