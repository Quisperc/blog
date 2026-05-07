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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
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

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (redisUtils.getZSet(MessageConstants.JWT_BLACKLISTS, token) != null) {
                sendError(response, MessageConstants.JWT_INVALID);
                return;
            }

            Claims claims = JwtTokenProvider.parserToken(token);
            String userId = claims.getSubject();

            List<SimpleGrantedAuthority> authorities = getAuthorities(userId, claims);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, token, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT 校验失败", e);
            sendError(response, MessageConstants.JWT_ERROR);
        }
    }

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

    private void sendError(HttpServletResponse response, String errorCode) throws IOException {
        Result error = Result.error(errorCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JSON.toJSONString(error));
    }
}