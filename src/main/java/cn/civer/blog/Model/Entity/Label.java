package cn.civer.blog.Model.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class Label implements Serializable {
    private BigInteger id;
    private BigInteger authorId;
    private String title;
    private Integer status = 1;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
