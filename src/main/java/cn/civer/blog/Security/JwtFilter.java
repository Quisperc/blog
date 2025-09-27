package cn.civer.blog.Security;

import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Utils.RedisUtils;
import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Jwt过滤器
 */
//@WebFilter(urlPatterns = "/*")
// public class JwtFilter implements Filter {
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private RedisUtils redisUtils;


    @Override
    // public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{
        // ServletRequest、ServletResponse是父类，
        // 请求对象与响应对象
//        HttpServletRequest request = (HttpServletRequest) request;
//        HttpServletResponse response = (HttpServletResponse) response;

        //  TODO 1.获取请求url
        String requestURL = request.getRequestURL().toString(); //不toString就是StringBuffer类型
        log.info("请求的url:{}", requestURL);

        //  TODO 2.判断请求url中是否包含login，如果包含，说明是登录操作，放行
        if (requestURL.contains("/login")||requestURL.contains("/register")){
            log.info("登录/注册操作，放行...");
            filterChain.doFilter(request, response);
            // 登录操作不需要执行下面的逻辑，直接结束此过滤器即可
            return;
        }

        //  TODO 3.获取请求头中的令牌（token）
        // 3. 先尝试从 Header 获取 token
        String token = request.getHeader("Authorization");
        if (StringUtils.hasLength(token) && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer "
        }

        if (!StringUtils.hasLength(token)) {
            token = request.getHeader("token");
        }

        // 4. 如果 Header 没有，再从 Cookie 获取 token
        if (!StringUtils.hasLength(token) && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        //  TODO 4.判断令牌是否存在，如果不存在，返回错误结果（未登录）
        if (!StringUtils.hasLength(token)) { //spring当中的工具类
            // 说明字符串为null，返回错误结果（未登录）
            log.info("请求头token为空，返回未登录的信息");
            Result error = Result.error("NOT_LOGIN");
            // 手动转JSON
            String errorJson = JSON.toJSONString(error);
            // response.getWriter()获取输出流，write()直接将数据响应给浏览器
            response.getWriter().write(errorJson);
            return;
        }

        //  TODO 5.解析token，如果解析失败，返回错误结果（未登录）
        //  说明存在令牌，校验
        try{
            // 1. 判断 token 是否在黑名单
            if (redisUtils.isExsits("jwt:blacklist:" + token)) {
                Result error = Result.error("Operate failed","TOKEN_INVALID");
                response.getWriter().write(JSON.toJSONString(error));
                return;
            }

            // 2. 解析 token
            Claims claims = JwtTokenProvider.parserToken(token);
            String userId = claims.getSubject();

            // 3. 从 Redis 获取权限
            String rolesJson = redisUtils.get("user:roles:" + userId);
            List<SimpleGrantedAuthority> authorities;
            if (rolesJson != null) {
                List<String> roles = JSON.parseArray(rolesJson, String.class);
                authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
            } else {
                // Redis 没有权限则从 token 获取
                List<String> roles = claims.get("roles", List.class);
                // 转换成 SimpleGrantedAuthority
                authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
                // 放入 Redis，过期时间和 JWT 保持一致，24 * 60 分钟
                redisUtils.set("user:roles:" + userId, JSON.toJSONString(roles), Duration.ofMinutes(24*60));
            }
//            // 权限列表配置
//            // 从 token 里取 roles
//            List<String> roles = claims.get("roles", List.class);
//            // 转换成 SimpleGrantedAuthority
//            List<SimpleGrantedAuthority> authorities = roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .toList();
            // 使用Spring Security配置权限
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, token, authorities);
            // 将封装好的authentication对象放入到程序上下文中
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authentication set: {}", SecurityContextHolder.getContext().getAuthentication());

        }catch (Exception e){ // 出现异常代表着解析失败
            e.printStackTrace();
            log.info("解析令牌失败，返回未登录错误信息");
            Result error = Result.error("NOT_LOGIN");
            // 手动转JSON
            String errorJson = JSON.toJSONString(error);
            // response.getWriter()获取输出流，write()直接将数据响应给浏览器
            response.getWriter().write(errorJson);
            return;
        }
        //  这里说明令牌解析成功，直接放行
        //  TODO 6.放行
        log.info("令牌合法，放行");
        filterChain.doFilter(request, response);
    }
}
