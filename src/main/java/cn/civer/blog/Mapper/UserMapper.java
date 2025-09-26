package cn.civer.blog.Mapper;

import cn.civer.blog.Entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;

@Mapper
public interface UserMapper {
    // 根据ID查询用户
    @Select("select * from t_user where id = #{id}")
    User selectById(@Param("id") BigInteger id);

    /**
     * 根据用户名username查询用户
     * @param username 用户名
     * @return 一个用户对象
     */
    @Select("select * from t_user where username = #{username}")
    User selectByUsername(@Param("username") String username);
    // 插入用户
    int insert(User user);
    // 删除用户
    int delete(@Param("id") BigInteger id);
    // 更新用户
    int update(User user);
}
