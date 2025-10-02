package cn.civer.blog.Controller.Admin;

import cn.civer.blog.Mapper.UserMapper;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Model.Enum.Role;
import cn.civer.blog.Service.UserServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/admin")
public class UserManagerController {
    @Autowired
    private UserServ userServ;


    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/delete")
    public Result userDelete(@RequestParam BigInteger userId){
        Result result = userServ.removeById(userId);
        return result;
    }

    @PreAuthorize("hasRole('manager')")
    @PutMapping("/update")
    public Result userUpdateByAdmin(@RequestParam BigInteger userId, @RequestParam Role role){
        return userServ.userUpdateByAdmin(userId,role);
    }

}
