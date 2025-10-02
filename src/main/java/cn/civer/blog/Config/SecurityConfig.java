package cn.civer.blog.Config;

import cn.civer.blog.Config.Security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Spring Security配置，用于通过权限验证
     * @param http
     * @param jwtFilter
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 登录注册接口放行
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                        // Swagger 相关接口（仅在非 prod 环境下放行）
                        .requestMatchers(getSwaggerWhitelist()).permitAll()
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                        //.anyRequest().permitAll()  // 临时放行所有请求
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    /**
     * Swagger 白名单路径
     */
    private String[] getSwaggerWhitelist() {
        return new String[]{
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",   // 可保留，兼容老版本
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico",
            "/.well-known/**"
        };
    }
}
