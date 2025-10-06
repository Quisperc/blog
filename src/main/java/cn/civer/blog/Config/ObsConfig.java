package cn.civer.blog.Config;

import cn.civer.blog.Config.Properties.ObsProperties;
import com.obs.services.ObsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// 显式启用配置属性
@EnableConfigurationProperties(ObsProperties.class)
@RequiredArgsConstructor
public class ObsConfig {
    private final ObsProperties obsProperties;
    // 注册并获取ObsClient实例
    @Bean
    public ObsClient obsClient() {
        return new ObsClient(obsProperties.getAccessKey(),
                           obsProperties.getSecretAccessKey(),
                           obsProperties.getEndpoint());
    }
}
