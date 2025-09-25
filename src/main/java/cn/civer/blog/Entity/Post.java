package cn.civer.blog.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name = "t_post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    // 标题
    @Column(nullable = false)
    private String title;
    // 作者
    @Column(nullable = false)
    private Long author;
    // 文章梗概
    @Column(nullable = false)
    private String summary;

    // 文章发布状态
    @Column(nullable = false)
    private Integer status; // 1 发布, 0 草稿

    // 浏览次数
    @Column(nullable = false)
    private Integer views = 0;

    // 点赞次数
    @Column(nullable = false)
    private Integer likes = 0;

    // 文章内容
    @Column(nullable = false)
    private String content;

    // 创建时间
    @Column(nullable = false)
    private LocalDateTime createTime;

    // 更新时间
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
