//package cn.civer.blog.Config;
//
//import cn.civer.blog.Model.Entity.MessageConstants;
//import cn.civer.blog.Model.Entity.Result;
//import cn.civer.blog.Utils.JwtTokenProvider;
//import cn.civer.blog.Utils.RedisUtils;
//import com.alibaba.fastjson2.JSON;
//import io.jsonwebtoken.Claims;
//import jakarta.servlet.*;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//
//import java.io.IOException;
//import java.time.Duration;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * JWT认证过滤器
// * 继承OncePerRequestFilter确保每个请求只过滤一次
// *
// * 主要功能：
// * 1. 对特定路径（登录、注册、Swagger等）进行放行
// * 2. 从请求头或Cookie中提取JWT令牌
// * 3. 验证令牌有效性（包括黑名单检查）
// * 4. 解析令牌并设置用户认证信息到SecurityContext
// *
// * @author Civer
// * @version 1.0
// */
//@Slf4j
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private RedisUtils redisUtils;
//
//    /**
//     * 不需要JWT认证的路径列表
//     * 包括：用户登录注册、Swagger文档接口等
//     */
//    private static final List<String> EXCLUDE_URLS = Arrays.asList(
////        "/api/user/login",           // 用户登录接口
////        "/api/user/register",        // 用户注册接口
////            "/api",
//            "/api/**",                 // ⭐ 统一放行 api
//        "/v3/api-docs",              // OpenAPI文档
//        "/v3/api-docs/**",           // OpenAPI文档所有子路径
//        "/swagger-ui/**",            // Swagger UI界面
//        "/swagger-ui.html",          // Swagger UI HTML页面
//        "/swagger-resources",        // Swagger资源
//        "/swagger-resources/**",     // Swagger资源所有子路径
//        "/webjars/**",                // WebJars静态资源
//        "/.well-known/**",
//        "/favicon.ico"
//    );
//
//    /**
//     * JWT过滤器核心方法
//     * 处理每个HTTP请求的JWT认证逻辑
//     *
//     * @param request HTTP请求对象
//     * @param response HTTP响应对象
//     * @param filterChain 过滤器链
//     * @throws ServletException 服务异常
//     * @throws IOException IO异常
//     */
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        // 步骤1: 获取请求URI路径
//        String requestURI = request.getRequestURI();
//        log.debug("处理请求路径: {}", requestURI);
//
//        // 步骤2: 检查是否为排除路径，如果是则直接放行
//        if (isExcludePath(requestURI)) {
//            log.debug("排除路径: {}, 直接放行", requestURI);
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 步骤3: 从请求中提取JWT令牌
//        String token = getTokenFromRequest(request);
//
//        // 步骤4: 验证令牌是否存在
//        if (!StringUtils.hasLength(token)) {
//            log.warn("请求未包含有效的JWT令牌: {}", requestURI);
//            sendError(response, MessageConstants.JWT_INVALID);
//            return;
//        }
//
//        try {
//            // 步骤5: 检查令牌是否在黑名单中（已注销）
////            if (redisUtils.hasKey(MessageConstants.JWT_BLACKLIST + token)) {
////                log.warn("令牌已在黑名单中: {}", token);
////                sendError(response, MessageConstants.JWT_INVALID);
////                return;
////            }
//            // 改用Sorted模式，减少 redis 缓存列表
//            if (redisUtils.getZSet(MessageConstants.JWT_BLACKLISTS,token) != null) {
//                log.warn("令牌已在黑名单中: {}", token);
//                sendError(response, MessageConstants.JWT_INVALID);
//                return;
//            }
//
//            // 步骤6: 解析JWT令牌获取声明信息
//            Claims claims = JwtTokenProvider.parserToken(token);
//            String userId = claims.getSubject();
//            log.debug("成功解析令牌，用户ID: {}", userId);
//
//            // 步骤7: 获取用户权限信息
//            List<SimpleGrantedAuthority> authorities = getAuthorities(userId, claims);
//
//            // 步骤8: 创建认证对象并设置到SecurityContext
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(userId, token, authorities);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            log.debug("用户认证信息设置完成，用户: {}，权限: {}", userId, authorities);
//
//            // 步骤9: 继续执行过滤器链
//            filterChain.doFilter(request, response);
//
//        } catch (Exception e) {
//            // 步骤10: 处理令牌解析异常
//            log.error("JWT令牌解析失败，路径: {}，异常: {}", requestURI, e.getMessage(), e);
//            sendError(response, MessageConstants.JWT_ERROR);
//        }
//    }
//
//    /**
//     * 检查请求路径是否在排除列表中
//     * 支持通配符匹配（/**）
//     *
//     * @param requestURI 请求URI路径
//     * @return true-需要排除，false-需要认证
//     */
//    private boolean isExcludePath(String requestURI) {
//        return EXCLUDE_URLS.stream().anyMatch(pattern -> {
//            // 处理通配符路径匹配
//            if (pattern.endsWith("/**")) {
//                String basePath = pattern.substring(0, pattern.length() - 3);
//                return requestURI.startsWith(basePath);
//            }
//            // 精确路径匹配
//            return requestURI.equals(pattern);
//        });
//    }
//
//    /**
//     * 从HTTP请求中提取JWT令牌
//     * 提取顺序：
//     * 1. Authorization请求头（Bearer Token）
//     * 2. 自定义token请求头
//     * 3. Cookie中的token
//     *
//     * @param request HTTP请求对象
//     * @return JWT令牌字符串，未找到返回null
//     */
//    private String getTokenFromRequest(HttpServletRequest request) {
//        String token;
//
//        // 方式1: 从Authorization头获取（标准Bearer Token）
//        token = request.getHeader("Authorization");
//        if (StringUtils.hasLength(token) && token.startsWith("Bearer ")) {
//            log.debug("从Authorization头获取令牌");
//            return token.substring(7); // 去掉"Bearer "前缀
//        }
//
//        // 方式2: 从自定义token头获取
//        token = request.getHeader("token");
//        if (StringUtils.hasLength(token)) {
//            log.debug("从自定义token头获取令牌");
//            return token;
//        }
//
//        // 方式3: 从Cookie中获取
//        if (request.getCookies() != null) {
//            for (Cookie cookie : request.getCookies()) {
//                if ("token".equals(cookie.getName())) {
//                    log.debug("从Cookie获取令牌");
//                    return cookie.getValue();
//                }
//            }
//        }
//
//        log.debug("未找到有效的JWT令牌");
//        return null;
//    }
//
//    /**
//     * 获取用户权限信息
//     * 优先从Redis缓存获取，缓存不存在则从JWT令牌解析并缓存
//     *
//     * @param userId 用户ID
//     * @param claims JWT声明信息
//     * @return 权限列表
//     */
//    private List<SimpleGrantedAuthority> getAuthorities(String userId, Claims claims) {
//        // 步骤1: 尝试从Redis缓存获取权限信息
//        String rolesJson = redisUtils.get(MessageConstants.JWT_USER_PRIVILEGE + userId);
//
//        if (rolesJson != null) {
//            // 步骤2: 缓存命中，解析权限列表
//            List<String> roles = JSON.parseArray(rolesJson, String.class);
//            log.debug("从Redis缓存获取用户权限，用户: {}，权限: {}", userId, roles);
//            return roles.stream()
//                    .map(SimpleGrantedAuthority::new)
//                    .toList();
//        } else {
//            // 步骤3: 缓存未命中，从JWT令牌解析权限
//            List<String> roles = claims.get("roles", List.class);
//            List<SimpleGrantedAuthority> authorities = roles.stream()
//                    .map(SimpleGrantedAuthority::new)
//                    .toList();
//
//            // 步骤4: 将权限信息缓存到Redis（24小时）
//            redisUtils.set(MessageConstants.JWT_USER_PRIVILEGE + userId, JSON.toJSONString(roles),
//                          Duration.ofMinutes(24 * 60));
//
//            log.debug("从JWT令牌解析权限并缓存，用户: {}，权限: {}", userId, roles);
//            return authorities;
//        }
//    }
//
//    /**
//     * 发送认证错误响应
//     * 设置JSON格式的错误信息返回给客户端
//     *
//     * @param response HTTP响应对象
//     * @param errorCode 错误代码
//     * @throws IOException IO异常
//     */
//    private void sendError(HttpServletResponse response, String errorCode) throws IOException {
//        // 创建错误结果对象
//        Result error = Result.error(errorCode);
//
//        // 转换为JSON字符串
//        String errorJson = JSON.toJSONString(error);
//
//        // 设置响应头
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        // 发送错误响应
//        response.getWriter().write(errorJson);
//
//        log.debug("发送认证错误响应，错误码: {}", errorCode);
//    }
//}
package cn.civer.blog.Config;

import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Utils.JwtTokenProvider;
import cn.civer.blog.Utils.RedisUtils;
import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private RedisUtils redisUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.debug("处理请求路径: {}", uri);

        // 1️⃣ 如果已经有认证信息，直接放行（避免重复解析）
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2️⃣ 获取 Token
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3️⃣ 黑名单校验
            if (redisUtils.getZSet(MessageConstants.JWT_BLACKLISTS, token) != null) {
                sendError(response, MessageConstants.JWT_INVALID);
                return;
            }

            // 4️⃣ 解析 Token
            Claims claims = JwtTokenProvider.parserToken(token);
            String userId = claims.getSubject();

            List<SimpleGrantedAuthority> authorities =
                    getAuthorities(userId, claims);

            // 5️⃣ 写入 SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, token, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT 校验失败", e);
            sendError(response, MessageConstants.JWT_ERROR);
        }
    }

    /** 从请求中获取 Token */
    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        token = request.getHeader("token");
        if (StringUtils.hasText(token)) {
            return token;
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /** 获取权限 */
    private List<SimpleGrantedAuthority> getAuthorities(String userId, Claims claims) {
        String rolesJson = redisUtils.get(MessageConstants.JWT_USER_PRIVILEGE + userId);
        if (rolesJson != null) {
            List<String> roles = JSON.parseArray(rolesJson, String.class);
            return roles.stream().map(SimpleGrantedAuthority::new).toList();
        }

        List<String> roles = claims.get("roles", List.class);
        redisUtils.set(
                MessageConstants.JWT_USER_PRIVILEGE + userId,
                JSON.toJSONString(roles),
                Duration.ofMinutes(24 * 60)
        );
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    /** 返回错误 */
    private void sendError(HttpServletResponse response, String errorCode) throws IOException {
        Result error = Result.error(errorCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JSON.toJSONString(error));
    }
}
