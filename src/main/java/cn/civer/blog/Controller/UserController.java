package cn.civer.blog.Controller;

import cn.civer.blog.Entity.Result;
import cn.civer.blog.Entity.User;
import cn.civer.blog.Service.UserServ;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserServ userServ;
    @PostMapping("/login")
    public Result userLogin(@RequestParam String username,@RequestParam String password
                            , HttpServletResponse response){
        // 获取登录结果
        Result<String> result = userServ.userLogin(username,password);
        String Jwt = result.getData();
        // todo 后期再改为Cookie，先使用header
//        // 写入 Cookie
//        Cookie cookie = new Cookie("token",Jwt);
//
//        cookie.setHttpOnly(true);      // 防止 JS 读取
//        cookie.setSecure(false);       // 生产环境用 true（只允许 HTTPS）
//        cookie.setPath("/");           // 整个站点都能访问
//        cookie.setMaxAge(60 * 60);     // 1 小时

        // 关键：SameSite=None 才能跨站携带
//        cookie.setComment("SameSite=None"); // 标准 API 里没有直接方法，需要改响应头
//        response.addHeader("Set-Cookie",
//        String.format("token=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None",
//                Jwt, 60 * 60));
//
//        response.addCookie(cookie);
        return result;
    }

    @PostMapping("/register")
    public Result userRegister(@RequestParam String username, @RequestParam String password){
        return userServ.userRegister(username,password);
    }

    @PreAuthorize("hasRole('manager')")
    @PostMapping("/delete")
    public Result userDelete(@RequestParam String username, HttpServletResponse response){
        Result result = userServ.removeByUsername(username);
        return result;
    }

    @PreAuthorize("hasRole('manager')")
    @PutMapping("/update")
    public Result userUpdate(@RequestBody User user){
        return userServ.userUpdate(user);
    }
    @PostMapping("/logout")
    public Result userUpdate(Authentication auth){
        // 获取已登录用户的凭证
        return userServ.userLogout(auth.getCredentials().toString());
    }
}
