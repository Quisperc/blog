package cn.civer.blog.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisUtils {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 存
    public void set(String key, String value, Duration duration){
        redisTemplate.opsForValue().set(key, value);
    }
    // 取
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    // 删
    public void remove(String key){
        redisTemplate.delete(key);
    }
    // 判断是否存在
    public boolean isExsits(String key){
        return redisTemplate.hasKey(key);
    }
}
