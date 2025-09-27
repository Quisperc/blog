package cn.civer.blog.Model.Entity;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Post {
    private BigInteger id;
    // 标题
    private String title;
    // 作者
    private BigInteger authorId;
    // 分类
    private List<Category> category;
    // 标签
    private List<Label> labels;
    // 文章梗概
    private String summary;
    // 文章发布状态
    private Integer status; // 1 发布, 0 草稿
    // 浏览次数
    private Integer views = 0;
    // 点赞次数
    private Integer likes = 0;
    // 文章内容
    private String content;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
}
