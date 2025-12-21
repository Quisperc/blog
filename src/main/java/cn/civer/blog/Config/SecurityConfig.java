package cn.civer.blog.Config;

import jakarta.servlet.DispatcherType;
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
//                        .requestMatchers("/api/user/login", "/api/user/register","/api").permitAll()
                        // ⭐ 放行所有 api 接口
//                        .requestMatchers(SecurityWhiteList.PUBLIC_URLS).permitAll()
                        // 其他接口需要认证
//                        .anyRequest().authenticated()
                        //.anyRequest().permitAll()  // 临时放行所有请求
                    // 1️⃣ 白名单接口
                    .requestMatchers(SecurityWhiteList.PUBLIC_URLS).permitAll()

                    // 2️⃣ 其他 api 接口必须登录
                    .requestMatchers(SecurityWhiteList.API_PREFIX).authenticated()

                    // 3️⃣ 其他全部拒绝（更安全）
                    .anyRequest().denyAll()
                    // 放行异步操作，确保流式文件异步下载正确
//                    .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    /**
     * Swagger 白名单路径
     */
//    private String[] getSwaggerWhitelist() {
//        return new String[]{
//            "/v3/api-docs/**",
//            "/swagger-ui/**",
//            "/swagger-ui.html",   // 可保留，兼容老版本
//            "/swagger-resources/**",
//            "/webjars/**",
//            "/favicon.ico",
//            "/.well-known/**"
//        };
//    }
}
