package cn.civer.blog.Security;

import cn.civer.blog.Entity.Result;
import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Jwt过滤器
 */
@Slf4j
@WebFilter(urlPatterns = "/*")
public class JwtFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // ServletRequest、ServletResponse是父类，
        // 请求对象与响应对象
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //  TODO 1.获取请求url
        String requestURL = request.getRequestURL().toString(); //不toString就是StringBuffer类型
        log.info("请求的url:{}", requestURL);

        //  TODO 2.判断请求url中是否包含login，如果包含，说明是登录操作，放行
        if (requestURL.contains("/login")){
            log.info("登录操作，放行...");
            filterChain.doFilter(request, response);
            // 登录操作不需要执行下面的逻辑，直接结束此过滤器即可
            return;
        }

        //  TODO 3.获取请求头中的令牌（token）
        String token = request.getHeader("token");

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
            Claims claims = JwtTokenProvider.parserToken(token);
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
