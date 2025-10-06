package cn.civer.blog.Utils;

import cn.civer.blog.Mapper.CategoryMapper;
import cn.civer.blog.Mapper.LabelMapper;
import cn.civer.blog.Mapper.PostCategoryMapper;
import cn.civer.blog.Mapper.PostLabelMapper;
import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
            if (c != null) categories.add(c);
        }

        // 组装 标签
        List<Label> labels = new ArrayList<>();
        for (BigInteger labelId : labelIds) {
            Label l = labelMapper.selectById(labelId);
            if (l != null) labels.add(l);
        }

        post.setCategorys(categories);
        post.setLabels(labels);
        return post;
    }

    /** 批量组装 */
    public List<Post> enrichPosts(List<Post> posts) {
        for (Post p : posts) enrichPost(p);
        return posts;
    }
}
