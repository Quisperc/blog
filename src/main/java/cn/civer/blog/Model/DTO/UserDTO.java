package cn.civer.blog.Model.DTO;

import cn.civer.blog.Model.Enum.Role;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

import lombok.Data;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
/**
 * 用户数据传输对象(Data Transfer Object)
 * 用于在系统各层之间传递用户相关数据
 * 实现了Serializable接口，支持序列化操作
 */
@Data
public class UserDTO implements Serializable {
    // ID
    private BigInteger id;
    // 用户名
    private String username;
    // 身份
    private Role role;
    // 注册时间
    private LocalDateTime createdAt;
    // 登录时间
    private LocalDateTime updatedAt;
}
