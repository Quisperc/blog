package cn.civer.blog.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    /**
     * 全局跨域配置
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许携带 Cookie
        config.addAllowedOrigin("http://localhost:8099"); // 前端地址
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedHeader("*");    // 允许所有请求头
        config.addAllowedMethod("*");    // 允许所有请求方法
        config.addExposedHeader("Authorization"); // 允许暴露Authorization头

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
