package cn.civer.blog.Model.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyFile {
    private BigInteger id;
    private String originName;
    private BigInteger authorId;
    private String objectKey;
    private LocalDateTime uploadTime;
}
