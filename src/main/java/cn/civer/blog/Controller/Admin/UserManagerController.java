package cn.civer.blog.Controller.Admin;

import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Model.Enum.Role;
import cn.civer.blog.Service.PostServ;
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
    @Autowired
    private PostServ postServ;

    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/delete")
    public Result userDelete(@RequestParam BigInteger userId){
        postServ.postDeleteByUserId(userId);
        userServ.removeById(userId);
        return Result.success(MessageConstants.USER_DELETE_SUCCESS);
    }

    @PreAuthorize("hasRole('manager')")
    @PutMapping("/update")
    public Result userUpdateByAdmin(@RequestParam BigInteger userId, @RequestParam Role role){
        userServ.userUpdateByAdmin(userId,role);
        return Result.success(MessageConstants.USER_UPDATE_SUCCESS);
    }

}
