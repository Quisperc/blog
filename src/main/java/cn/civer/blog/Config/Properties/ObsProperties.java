package cn.civer.blog.Config.Properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "huawei.obs")
public class ObsProperties {
    private String endpoint;
    private String accessKey;
    private String secretAccessKey;
    private String bucketName;
    private Long expiration;
    private String accessUrl;
}
