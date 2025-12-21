package cn.civer.blog.Service;

import cn.civer.blog.Model.DTO.UserDTO;
import cn.civer.blog.Model.Entity.User;
import cn.civer.blog.Model.Enum.Role;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface UserServ {
    // 用户注册
    // 用户登录
    // 用户注销
    // 用户信息更新
    List<UserDTO> getUsers();
    UserDTO getById(BigInteger id);
    Boolean removeById(BigInteger id);
    //Result removeByUsername(String username);
    Boolean userRegister(String username, String rawPassword);
    Boolean userUpdateByUser(String username, String password);
    Map<String, Object> userLogin(String username, String rawPassword);
    Boolean userLogout(String token);

    Boolean userUpdateByAdmin(BigInteger userId, Role role);
}
