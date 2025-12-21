package cn.civer.blog.Controller.User;

import cn.civer.blog.Model.DTO.UserDTO;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.UserServ;
import cn.civer.blog.Utils.ObsUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserServ userServ;
    @PostMapping("/login")
    public ResponseEntity<Result<?>> userLogin(@RequestParam("username") String username,
                            @RequestParam("password") String password){
        // 1️⃣ 调用 Service 登录，返回 Map 包含 token 和用户数据
        Map<String, Object> result = userServ.userLogin(username, password);

        if (result != null && result.get("data") != null && result.get("token") != null) {

            // 2️⃣ 设置 Header 返回 token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + result.get("token"));

            // 3️⃣ 返回 ResponseEntity 包含 Result 和 Header
            Result<?> res = Result.success(result.get("data")); // 显式指定泛型、
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(res);
        }

        // 4️⃣ 登录失败返回 401 + 错误信息
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.errorMsg(MessageConstants.USER_LOGIN_FAILED));
    }

    @PostMapping("/register")
    public Result userRegister(@RequestParam("username") String username,
                               @RequestParam("password") String password){
        userServ.userRegister(username,password);
        return Result.success(MessageConstants.USER_INSERT_SUCCESS);
    }

    @PreAuthorize("hasRole('viewer')")
    @PutMapping("/update")
    public Result userUpdateByUser(@RequestParam("username") String username,
                                   @RequestParam("password") String password){
        userServ.userUpdateByUser(username,password);
        return Result.success(MessageConstants.USER_UPDATE_SUCCESS);
    }

    @PreAuthorize("hasRole('viewer')")
    @PostMapping("/logout")
    public Result userLogout(Authentication auth){
        // 获取已登录用户的凭证
        userServ.userLogout(auth.getCredentials().toString());
        return Result.success(MessageConstants.USER_LOGOUT);
    }
}
