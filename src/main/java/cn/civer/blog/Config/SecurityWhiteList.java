package cn.civer.blog.Config;

public class SecurityWhiteList {

    /** 完全匿名可访问 */
    public static final String[] PUBLIC_URLS = {
        "/api/user/login",
        "/api/user/register",

        // 文章浏览类接口
        "/api/post/**",
        "/api/category/**",
        "/api/label/**",
        "/api/admin/label/**",
        "/api/admin/category/**",

        // Swagger
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/webjars/**",
        "/favicon.ico",
        "/.well-known/**"
    };

    /** API 前缀 */
    public static final String API_PREFIX = "/api/**";
}

