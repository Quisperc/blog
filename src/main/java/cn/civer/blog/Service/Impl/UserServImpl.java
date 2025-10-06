package cn.civer.blog.Service.Impl;

import cn.civer.blog.Exception.BizException;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Enum.Role;
import cn.civer.blog.Model.Entity.User;
import cn.civer.blog.Mapper.UserMapper;
import cn.civer.blog.Utils.JwtTokenProvider;
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
     *
     * @param id 用户ID
     * @return 返回结果
     */
    @Override
    public User getById(BigInteger id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(MessageConstants.USER_NOT_EXIST+": " + id);
        }
        return user;
    }

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return 返回结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean removeById(BigInteger id) {
        User user = userMapper.selectById(id);
        // 判断用户是否存在
        if (user == null) {
            throw new BizException(MessageConstants.USER_NOT_EXIST + ": {}" + id);
        }
        // 移除用户
        userMapper.deleteById(id);
        log.info(MessageConstants.USER_DELETE_SUCCESS + ": {}) ", user.getUsername());
        return Boolean.TRUE;
    }

    /**
     * 用户注册
     *
     * @param username    用户名
     * @param rawPassword 原始密码
     * @return 注册结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean userRegister(String username, String rawPassword) {
        User userSelect =  userMapper.selectByUsername(username);
        // 判断用户是否存在
        if (userSelect != null) {
            // 存在则无法注册
            throw new BizException(MessageConstants.USER_EXIST + ": {}" + username);
        }
        // 填写新用户表单信息
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtils.encode(rawPassword));
        user.setRole(Role.viewer);
        user.setRegisterTime(LocalDateTime.now());
        userMapper.insert(user);
        // 日志记录
        log.info(MessageConstants.USER_INSERT_SUCCESS + ": {}", username);
        return Boolean.TRUE;
    }


    /**
     * 用户更新
     *
     * @param username 用户名
     * @param password 密码
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean userUpdateByUser(String username, String password) {
        // 校验权限
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BigInteger userId = new BigInteger(auth.getName());
        // 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(MessageConstants.USER_NOT_EXIST);
        }

        // 判断用户是否存在
        User userSelect =  userMapper.selectByUsername(username);
        if (userSelect != null) {
            // 存在则无法注册
            throw new BizException(MessageConstants.USER_EXIST + ": {}" + username);
        }

        // 用户名不为空则表明需要修改用户名
        if (!"".equals(username)) {
            user.setUsername(username);
        }
        // 密码不为空则加密后更新
        if (!"".equals(password)) {
            user.setPassword(PasswordUtils.encode(password));
        }
        // 更新用户
        userMapper.update(user);
        log.info(MessageConstants.USER_UPDATE_SUCCESS+": "+username);
        return Boolean.TRUE;
    }

    /**
     * 用户登录
     *
     * @param username    用户名
     * @param rawPassword 原始密码
     * @return Result包jwt
     */
    @Override
    public String userLogin(String username, String rawPassword) {
        // 获取User对象
        User user = userMapper.selectByUsername(username);
        // 判断用户是否存在
        if (user == null) {
            throw new BizException(MessageConstants.USER_NOT_EXIST);
        }

        // 备用
        Map<String, Object> mp = new HashMap<>();

        // 校验密码
        // 将密码加密后与获取到的密码进行匹配
        if (PasswordUtils.matches(rawPassword, user.getPassword())) {
            // 更新登录时间
            user.setLoginTime(LocalDateTime.now());
            // 获取用户权限
            List<String> priList = PrivilegeUtils.getPri(user);
            // 匹配成功则生成jwt并返回登录成功消息
            String jwt = JwtTokenProvider.generateToken(user.getId(), username, priList, mp);
            // 存入 redis
            redisUtils.set(MessageConstants.JWT_USER_PRIVILEGE + user.getId(), JSON.toJSONString(priList), Duration.ofMinutes(24 * 60));

            log.info(MessageConstants.USER_LOGIN_SUCCESS+"({})！权限为" + priList, user.getUsername());
            return jwt;
        } else {
            throw new BizException(MessageConstants.USER_LOGIN_FAILED);
        }
    }

    /**
     * 用户退出登录
     *
     * @param token jwt
     * @return 退出成功结果
     */
    @Override
    public Boolean userLogout(String token) {
        // 加入黑名单，24*60保证jwt完全失效
        redisUtils.set(MessageConstants.JWT_BLACKLIST + token, "true", Duration.ofMinutes(24 * 60)); // 和 JWT 保持一致
        log.info("退出成功！token：" + token + "已失效！");
        return Boolean.TRUE;
    }

    /**
     * 更新用户角色
     *
     * @param userId 用户Id
     * @param role   用户角色
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean userUpdateByAdmin(BigInteger userId, Role role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(MessageConstants.USER_NOT_EXIST);
        }
        // 设置用户角色
        user.setRole(role);
        userMapper.update(user);
        log.info(MessageConstants.USER_UPDATE_SUCCESS+": "+ userId);
        return Boolean.TRUE;
    }
}
