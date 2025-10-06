package cn.civer.blog.Model.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class File {
    private BigInteger id;
    private String originName;
    private String authorId;
    private String objectKey;
    private LocalDateTime uploadTime;
}
