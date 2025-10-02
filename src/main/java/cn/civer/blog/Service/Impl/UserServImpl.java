package cn.civer.blog.Service.Impl;

import cn.civer.blog.Model.Enum.Role;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Model.Entity.User;
import cn.civer.blog.Mapper.UserMapper;
import cn.civer.blog.Config.Security.JwtTokenProvider;
import cn.civer.blog.Service.UserServ;
import cn.civer.blog.Utils.PasswordUtils;
import cn.civer.blog.Utils.PrivilegeUtils;
import cn.civer.blog.Utils.RedisUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional  // MyBatis也需要事务管理
public class UserServImpl implements UserServ {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 根据ID返回用户
     * @param id 用户ID
     * @return 返回结果
     */
    @Override
    public Result<?> getById(BigInteger id) {
        User user = userMapper.selectById(id);
        if(user == null){
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 返回结果
     */
    @Override
    public Result removeById(BigInteger id) {
        User user = userMapper.selectById(id);
        // 判断用户是否存在
        if(user==null){
            log.info("用户("+id+") 删除失败！用户不存在");
            return Result.error("用户不存在！");
        }
        try {
            userMapper.deleteById(id);
            log.info("用户("+user.getUsername()+") 删除成功！");
            return Result.success("用户已删除！");
        } catch (Exception e) {
            log.info("用户("+user.getUsername()+") 删除失败！");
            return Result.error("用户未删除！未知错误！");
        }
    }

    /**
     * 根据用户名删除用户
     * @param username 用户名
     * @return 删除结果
     */
//    @Override
//    public Result removeByUsername(String username) {
//        User user = userMapper.selectByUsername(username);
//        // 判断用户是否存在
//        if(user==null){
//            log.info("用户("+username+") 删除失败！用户不存在");
//            return Result.error("用户不存在！");
//        }
//        try {
//            userMapper.deleteByUsername(username);
//
//
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            log.info("Current auth: {}", auth);
//
//            log.info("用户("+user.getUsername()+") 删除成功！");
//            return Result.success("用户已删除！");
//        } catch (Exception e) {
//            log.info("用户("+user.getUsername()+") 删除失败！");
//            return Result.error("用户未删除！未知错误！");
//        }
//    }

    /**
     * 用户注册
     * @param username 用户名
     * @param rawPassword 原始密码
     * @return 注册结果
     */
    @Override
    public Result<String> userRegister(String username, String rawPassword) {
        // 填写新用户表单信息
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtils.encode(rawPassword));
        user.setRole(Role.viewer);
        user.setRegisterTime(LocalDateTime.now());
        try{
            // 尝试注册
//            if(userMapper.selectByUsername(username) != null){
//                log.info("用户("+user.getUsername()+") 注册失败！");
//                return Result.error("用户已存在！");
//            }
            userMapper.insert(user);

            // 日志记录
            log.info("用户("+user.getUsername()+") 注册成功！");
            return Result.success("用户注册成功！");
        }catch(Exception ex){
            log.info("用户("+user.getUsername()+") 注册失败！未知错误！");
            return Result.error("未知错误");
        }
    }


    /**
     * 用户更新
     * @param username 用户名
     * @param password 密码
     * @return 更新结果
     */
    @Override
    public Result userUpdateByUser(String username,String password) {
        // 校验权限
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BigInteger userId = new BigInteger(auth.getName());
        // 校验用户是否存在
        User user = userMapper.selectById(userId);
        if(user == null){
            return Result.error("用户不存在！");
        }
        try {
            // 用户名不为空则表明需要修改用户名
            if(!"".equals(username)){
                user.setUsername(username);
            }
            // 密码不为空则加密后更新
            if(!"".equals(user.getPassword())){
                user.setPassword(PasswordUtils.encode(user.getPassword()));
            }
            // 更新用户
            userMapper.update(user);
            return Result.success("用户信息已更新！");
        } catch (Exception e) {
            return Result.error("未知错误！");
        }
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
            return Result.error("用户不存在");
        }

        // 备用
        Map<String,Object> mp = new HashMap<>();

        // 校验密码
        try {
            // 将密码加密后与获取到的密码进行匹配
            if(PasswordUtils.matches(rawPassword,user.getPassword())) {
                // 更新登录时间
                user.setLoginTime(LocalDateTime.now());
                // 获取用户权限
                List priList =  PrivilegeUtils.getPri(user);
                // 匹配成功则生成jwt并返回登录成功消息
                String jwt = JwtTokenProvider.generateToken(user.getId(),username,priList,mp);
                // 存入 redis
                redisUtils.set("user:roles:" + user.getId(), JSON.toJSONString(priList), Duration.ofMinutes(24*60));

                log.info("用户("+user.getUsername()+") 登录成功！权限为"+priList);
                return Result.success(jwt);
            }else{
                log.info("用户("+user.getUsername()+") 登录失败！密码错误！");
                return Result.error("密码错误");
            }
        } catch (Exception e) {
            log.info("用户("+user.getUsername()+") 登录失败！未知错误！");
            return Result.error();
        }
    }

    @Override
    public Result userLogout(String token) {
        // 加入黑名单，24*60保证jwt完全失效
        redisUtils.set("jwt:blacklist:" + token, "true", Duration.ofMinutes(24*60)); // 和 JWT 保持一致
        log.info("退出成功！token："+token+"已失效！");
        return Result.success("用户退出");
    }

    @Override
    public Result userUpdateByAdmin(BigInteger userId, Role role) {
        try {
            User user = userMapper.selectById(userId);
            if(user == null){
                log.info("用户不存在");
                return Result.error("用户不存在");
            }
            user.setRole(role);
            userMapper.update(user);
            return Result.success("修改用户角色成功");
        } catch (Exception e) {
            log.error("修改失败："+e);
            return Result.error("修改失败");
        }
    }
}
