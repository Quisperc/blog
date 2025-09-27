package cn.civer.blog.Model.DTO;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class PostCategoryDTO {
    private BigInteger id;
    private BigInteger postId;
    private BigInteger categoryId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
