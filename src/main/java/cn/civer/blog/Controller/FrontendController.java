package cn.civer.blog.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端路由控制器
 * 处理所有非API请求，返回前端入口页面
 */
@Controller
public class FrontendController {

    /**
     * 处理所有非API请求，返回前端入口页面
     * 这样可以支持直接访问前端路由
     */
    @GetMapping(value = {"/categories", "/labels", "/gallery", "/login", "/register", "/profile", "/admin", 
                         "/category/**", "/label/**", "/post/**"})
    public String forwardToFrontend() {
        // 返回前端入口页面，让React Router接管路由处理
        return "forward:/index.html";
    }
}