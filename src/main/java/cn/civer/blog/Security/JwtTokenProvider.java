package cn.civer.blog.Security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    // 密钥
    @Value("${jwt.secret}")
    private static String secret;

    // 过期时间
    @Value("${jwt.expiration-ms}")
    private static long expirationMs;

    /**
     * 生成JwtToken
     * @param userID 用户ID
     * @param username 用户名
     * @param Claims 验证声明
     * @return 生成的token
     */
    public static String generateToken(Long userID, String username, Map<String, Object> Claims){
        JwtBuilder builder = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, secret) // 签名
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) // 过期时间
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
                    .setSigningKey(secret)  // 用secret解密
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
