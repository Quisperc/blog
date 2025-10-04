package cn.civer.blog.Model.Entity;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class Category {
    private BigInteger id;
    private BigInteger authorId;
    private String title;
    private String summary;
    private Integer status = 1;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
