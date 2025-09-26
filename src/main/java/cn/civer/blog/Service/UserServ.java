package cn.civer.blog.Service;

import cn.civer.blog.Entity.Result;
import cn.civer.blog.Entity.User;

import java.math.BigInteger;

public interface UserServ {
    // 用户注册
    // 用户登录
    // 用户注销
    // 用户信息更新
    Result getById(BigInteger id);
    Result removeById(BigInteger id);
    Result removeByUsername(String username);
    Result<String> userRegister(String username, String rawPassword);
    Result userUpdate(User user);
    Result<String> userLogin(String username, String rawPassword);
}
