package cn.civer.blog.Controller;

import cn.civer.blog.Entity.Result;
import cn.civer.blog.Service.UserServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserServ userServ;
    @PostMapping("/login")
    public Result userLogin(@RequestParam String username,@RequestParam String password){
        return userServ.userLogin(username,password);
    }

    @PostMapping("/register")
    public Result<String> userRegister(@RequestParam String username, @RequestParam String password){
        return userServ.userRegister(username,password);
    }
}
