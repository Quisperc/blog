package cn.civer.blog.Model.Entity;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class Label {
    private BigInteger id;
    private BigInteger authorId;
    private String title;
    private String summary;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
