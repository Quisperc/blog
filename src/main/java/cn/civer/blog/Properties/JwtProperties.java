package cn.civer.blog.Properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    // 密钥
    //@Value("${jwt.secret}")
    private String secret;

    // 过期时间
    //@Value("${jwt.expiration-ms}")
    private long expirationMs;
}
