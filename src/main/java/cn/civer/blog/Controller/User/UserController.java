package cn.civer.blog.Controller.User;

import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.UserServ;
import cn.civer.blog.Utils.ObsUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserServ userServ;
    @PostMapping("/login")
    public Result userLogin(@RequestParam("username") String username,@RequestParam("password") String password){
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
