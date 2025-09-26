package cn.civer.blog.Service.Impl;

import cn.civer.blog.Entity.Result;
import cn.civer.blog.Entity.User;
import cn.civer.blog.Entity.Role;
import cn.civer.blog.Mapper.UserMapper;
import cn.civer.blog.Security.JwtTokenProvider;
import cn.civer.blog.Service.UserServ;
import cn.civer.blog.Utils.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional  // MyBatis也需要事务管理
public class UserServImpl implements UserServ {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordUtils passwordUtils;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public User getById(BigInteger id) {
        return userMapper.selectById(id);
    }

    @Override
    public int removeById(BigInteger id) {

        return userMapper.delete(id);
    }


    @Override
    public Result<String> userRegister(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtils.encode(rawPassword));
        user.setRole(Role.viewer);
        user.setRegisterTime(LocalDateTime.now());
        // 获取注册时间
        //  LocalDateTime now = LocalDateTime.now();
        //  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //  String formattedNow = now.format(formatter);
        try{
            if(userMapper.selectByUsername(username) != null){
                log.info("用户("+user.getUsername()+") 注册失败！");
                return Result.error("注册失败","用户已存在！");
            }
            userMapper.insert(user);

            log.info("用户("+user.getUsername()+") 注册成功！");
            return Result.Success("注册成功","用户注册成功！");
        }catch(Exception ex){
            log.info("用户("+user.getUsername()+") 注册失败！未知错误！");
            return Result.error("注册失败","未知错误");
        }
    }

    @Override
    public int userUpdate(User user) {

        return userMapper.update(user);
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param rawPassword 原始密码
     * @return Result包jwt
     */
    @Override
    public Result<String> userLogin(String username, String rawPassword) {
        // 获取User对象
        User user = userMapper.selectByUsername(username);
        // 判断用户是否存在
        if(user == null){
            log.info("用户("+username+")登录失败！用户不存在");
            return Result.error("登录失败","用户不存在");
        }

        // 备用
        Map<String,Object> mp = new HashMap<>();

        // 校验密码
        try {
            String encodePassword = user.getPassword();
            BigInteger userID = user.getId();
            // 将密码加密后与获取到的密码进行匹配
            if(PasswordUtils.matches(rawPassword,encodePassword)) {
                // 匹配成功则生成jwt并返回登录成功消息
                String jwt = JwtTokenProvider.generateToken(userID,username,mp);
                // 更新登录时间
                LocalDateTime now = LocalDateTime.now();
                user.setLoginTime(now);
                log.info("用户("+user.getUsername()+") 登录成功！");
                return Result.Success("登录成功", "jwt为"+jwt);
            }else{
                log.info("用户("+user.getUsername()+") 登录失败！密码错误！");
                return Result.error("登录失败","密码错误");
            }
        } catch (Exception e) {
            return Result.error();
        }
    }
}
