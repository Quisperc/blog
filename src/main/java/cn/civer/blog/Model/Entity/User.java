package cn.civer.blog.Model.Entity;

import cn.civer.blog.Model.Enum.Role;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
    // ID
    private BigInteger id;
    // 用户名
    private String username;
    // 密码
    private String password;
    // 身份
    private Role role;
    // 注册时间
    private LocalDateTime createdAt;
    // 登录时间
    private LocalDateTime updatedAt;
}
