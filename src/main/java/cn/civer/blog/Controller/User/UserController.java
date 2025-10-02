package cn.civer.blog.Controller.User;

import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.UserServ;
import jakarta.servlet.http.HttpServletResponse;
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
        return userServ.userLogin(username,password);
    }

    @PostMapping("/register")
    public Result userRegister(@RequestParam String username, @RequestParam String password){
        return userServ.userRegister(username,password);
    }

    @PreAuthorize("hasRole('viewer')")
    @PutMapping("/update")
    public Result userUpdateByUser(@RequestParam String username, @RequestParam String password){
        return userServ.userUpdateByUser(username,password);
    }

    @PostMapping("/logout")
    public Result userUpdate(Authentication auth){
        // 获取已登录用户的凭证
        return userServ.userLogout(auth.getCredentials().toString());
    }
}
