package cn.civer.blog.Utils;

import cn.civer.blog.Mapper.CategoryMapper;
import cn.civer.blog.Mapper.LabelMapper;
import cn.civer.blog.Mapper.PostCategoryMapper;
import cn.civer.blog.Mapper.PostLabelMapper;
import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
@Slf4j
@Component
public class PostAssembler {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private LabelMapper labelMapper;
    @Autowired
    private PostCategoryMapper postCategoryMapper;
    @Autowired
    private PostLabelMapper postLabelMapper;

    /** 填充文章的分类与标签信息 */
    public Post enrichPost(Post post) {
        List<BigInteger> categoryIds = postCategoryMapper.selectByPostId(post.getId());
        List<BigInteger> labelIds = postLabelMapper.selectByPostId(post.getId());

        // 组装 分类
        List<Category> categories = new ArrayList<>();
        for (BigInteger categoryId : categoryIds) {
            Category c = categoryMapper.selectById(categoryId);
            if (c != null) {
                categories.add(c);
                log.info(MessageConstants.POST_CATEGORY_SELECT_SUCCESS+": 绑定分类 [{}] -> 文章 [{}]", c.getTitle(), post.getTitle());
            }
        }

        // 组装 标签
        List<Label> labels = new ArrayList<>();
        for (BigInteger labelId : labelIds) {
            Label l = labelMapper.selectById(labelId);
            if (l != null){
                labels.add(l);
                log.info(MessageConstants.POST_LABEL_SELECT_SUCCESS+": 绑定标签 [{}] -> 文章 [{}]", l.getTitle(), post.getTitle());
            }
        }

        post.setCategorys(categories);
        post.setLabels(labels);
        return post;
    }

    /** 批量组装 */
    public List<Post> enrichPosts(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return posts;

        // 收集所有文章ID
        List<BigInteger> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        // 批量查询文章->分类/标签映射
        List<cn.civer.blog.Model.DTO.PostCategoryDTO> postCategoryDTOS = postCategoryMapper.selectByPostIds(postIds);
        List<cn.civer.blog.Model.DTO.PostLabelDTO> postLabelDTOS = postLabelMapper.selectByPostIds(postIds);

        // 构建 postId -> categoryId list 映射
        Map<BigInteger, List<BigInteger>> postToCategoryIds = new HashMap<>();
        for (cn.civer.blog.Model.DTO.PostCategoryDTO dto : postCategoryDTOS) {
            postToCategoryIds.computeIfAbsent(dto.getPostId(), k -> new ArrayList<>()).add(dto.getCategoryId());
        }

        // 构建 postId -> labelId list 映射
        Map<BigInteger, List<BigInteger>> postToLabelIds = new HashMap<>();
        for (cn.civer.blog.Model.DTO.PostLabelDTO dto : postLabelDTOS) {
            postToLabelIds.computeIfAbsent(dto.getPostId(), k -> new ArrayList<>()).add(dto.getLabelId());
        }

        // 收集所有需要的 categoryIds / labelIds
        Set<BigInteger> allCategoryIds = postCategoryDTOS.stream().map(c -> c.getCategoryId()).collect(Collectors.toCollection(HashSet::new));
        Set<BigInteger> allLabelIds = postLabelDTOS.stream().map(l -> l.getLabelId()).collect(Collectors.toCollection(HashSet::new));

        // 批量查询实体
        List<Category> categories = allCategoryIds.isEmpty() ? new ArrayList<>() : categoryMapper.selectByIds(new ArrayList<>(allCategoryIds));
        List<Label> labels = allLabelIds.isEmpty() ? new ArrayList<>() : labelMapper.selectByIds(new ArrayList<>(allLabelIds));

        // 构建 id -> entity 映射
        Map<BigInteger, Category> categoryById = categories.stream().collect(Collectors.toMap(Category::getId, c -> c));
        Map<BigInteger, Label> labelById = labels.stream().collect(Collectors.toMap(Label::getId, l -> l));

        // 为每篇文章填充 categorys / labels
        for (Post post : posts) {
            List<BigInteger> cids = postToCategoryIds.get(post.getId());
            List<Category> cs = new ArrayList<>();
            if (cids != null) {
                for (BigInteger cid : cids) {
                    Category c = categoryById.get(cid);
                    if (c != null) {
                        cs.add(c);
                        log.info(MessageConstants.POST_CATEGORY_SELECT_SUCCESS + ": 绑定分类 [{}] -> 文章 [{}]", c.getTitle(), post.getTitle());
                    }
                }
            }
            post.setCategorys(cs);

            List<BigInteger> lids = postToLabelIds.get(post.getId());
            List<Label> ls = new ArrayList<>();
            if (lids != null) {
                for (BigInteger lid : lids) {
                    Label l = labelById.get(lid);
                    if (l != null) {
                        ls.add(l);
                        log.info(MessageConstants.POST_LABEL_SELECT_SUCCESS + ": 绑定标签 [{}] -> 文章 [{}]", l.getTitle(), post.getTitle());
                    }
                }
            }
            post.setLabels(ls);
        }

        return posts;
    }
}
