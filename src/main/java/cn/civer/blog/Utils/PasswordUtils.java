package cn.civer.blog.Utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
    // 获取密码加密器
    private static final BCryptPasswordEncoder Byencoder = new BCryptPasswordEncoder();

    /**
     * 对密码进行加密
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return Byencoder.encode(rawPassword);
    }

    /**
     * 比较密码是否匹配
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 匹配结果
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return Byencoder.matches(rawPassword, encodedPassword);
    }
}
