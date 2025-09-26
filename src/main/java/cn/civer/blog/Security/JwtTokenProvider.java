package cn.civer.blog.Security;

import cn.civer.blog.Properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@Data
// @ConfigurationProperties(prefix = "jwt")
public class JwtTokenProvider {
    private static JwtProperties jwtProperties;
    @Autowired
    public void setJwtProperties(JwtProperties jwtProperties) {
        JwtTokenProvider.jwtProperties = jwtProperties; // 注入时写入静态字段
    }
    /**
     * 生成JwtToken
     * @param userID 用户ID
     * @param username 用户名
     * @param Claims 验证声明
     * @return 生成的token
     */
    public static String generateToken(BigInteger userID, String username, Map<String, Object> Claims){
        JwtBuilder builder = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()), SignatureAlgorithm.HS256) // 签名
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs())) // 过期时间
                .claim("username",username) // 声明username
                .setSubject(String.valueOf(userID))  // 主题：用户ID
                .setIssuedAt(new Date());            // 签发时间：现在
        return builder.compact();
    }


    /**
     * 获取声明解析结果
     * @param Token jwt token
     * @return
     */
    public static Claims parserToken(String Token){
        return Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret())  // 用secret解密
                    .parseClaimsJws(Token) // 解析jwt
                    .getBody(); // 获取所有声明
    }

    /**
     * 校验jwt是否有效
     * @param jwttoken 传入的jwt token
     * @return 验证结果true or false
     */
    public static boolean isTokenValid(String jwttoken){
        try {
            Claims c = parserToken(jwttoken);
            return c.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
//            throw new RuntimeException(ex);
            return false;
        }
    }
}
