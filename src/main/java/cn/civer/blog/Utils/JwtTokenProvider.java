package cn.civer.blog.Utils;

import cn.civer.blog.Config.Properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Autowired
    private JwtProperties jwtProperties;

    private static JwtTokenProvider instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    private static JwtTokenProvider getInstance() {
        return instance;
    }
    /**
     * 生成JwtToken
     * @param userID 用户ID
     * @param username 用户名
     * @param Claims 验证声明
     * @return 生成的token
     */
    public static String generateToken(BigInteger userID, String username, List<?> arrayList, Map<String, Object> claims) {
        JwtProperties props = getInstance().jwtProperties;
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(props.getSecret().getBytes()), SignatureAlgorithm.HS256)
                .setExpiration(new Date(System.currentTimeMillis() + props.getExpirationMs()))
                .claim("username", username)
                .claim("roles", arrayList)
                .setSubject(String.valueOf(userID))
                .setIssuedAt(new Date())
                .compact();
    }

    public static Claims parserToken(String token) {
        JwtProperties props = getInstance().jwtProperties;
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(props.getSecret().getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
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
