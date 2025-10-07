package cn.civer.blog.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 给一个指定的 key 值附加过期时间
     * @param key 键
     * @param duration 持续时间
     * @return 是否设置成功
     */
    public Boolean expire(String key, Duration duration) {
        return redisTemplate.expire(key, duration);
    }
    /**
     * 根据key 获取过期时间
     * @param key key
     * @return 时间
     */
    public Duration getTime(String key) {
        return Duration.ofMinutes(redisTemplate.getExpire(key));
    }
    /**
     * 判断Key是否存在
     * @param key key
     * @return true or false
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    /**
     * 移除指定key 的过期时间
     * @param key key
     * @return true or false
     */
    public boolean persist(String key) {
        return Boolean.TRUE.equals(redisTemplate.boundValueOps(key).persist());
    }

//- - - - - - - - - - - - - - - - - - - - -  String类型 - - - - - - - - - - - - - - - - - - - -
    /**
     * 将值放入缓存
     * @param key   键
     * @param value 值
     */
    public void set(String key, String value){
        redisTemplate.opsForValue().set(key, value);
    }
    /**
     * 将值放入缓存并设置过期时间
     * @param key 键
     * @param value 值
     * @param duration 持续时间
     */
    public void set(String key, String value, Duration duration){
        redisTemplate.opsForValue().set(key, value, duration);
    }
    /**
     * 根据key获取值
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    // 删
    public void remove(String key){
        redisTemplate.delete(key);
    }

    /**
     * 批量添加 key (重复的键会覆盖)
     * @param keyAndValue Map存储 键值对
     */
    public void batchSet(Map<String, String> keyAndValue) {
        redisTemplate.opsForValue().multiSet(keyAndValue);
    }

    /**
     * 批量添加 key-value 只有在键不存在时,才添加
     * map 中只要有一个key存在,则全部不添加
     * @param keyAndValue Map存储 键值对
     */
    public void batchSetIfAbsent(Map<String, String> keyAndValue) {
        redisTemplate.opsForValue().multiSetIfAbsent(keyAndValue);
    }

    /**
     * 对一个 key-value 的值进行加减操作,
     * 如果该 key 不存在 将创建一个key 并赋值该 number
     * 如果 key 存在,但 value 不是长整型 ,将报错
     * @param key 键
     * @param number long类型 值
     */
    public Long increment(String key, long number) {
        return redisTemplate.opsForValue().increment(key, number);
    }

    /**
     * 对一个 key-value 的值进行加减操作,
     * 如果该 key 不存在 将创建一个key 并赋值该 number
     * 如果 key 存在,但 value 不是 纯数字 ,将报错
     * @param key 键
     * @param number double类型 值
     */
    public Double increment(String key, double number) {
        return redisTemplate.opsForValue().increment(key, number);
    }

    //- - - - - - - - - - - - - - - - - - - - -  set类型 - - - - - - - - - - - - - - - - - - - -

    /**
     * 将数据放入set缓存
     * @param key 键
     * @param value 值
     */
    public void sSet(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 获取变量中的值
     * @param key 键
     * @return 集合中的所有元素
     */
    public Set<String> members(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 随机获取变量中指定个数的元素
     * @param key   键
     * @param count 值
     */
    public void randomMembers(String key, long count) {
        redisTemplate.opsForSet().randomMembers(key, count);
    }

    /**
     * 随机获取变量中的元素
     * @param key 键
     * @return Set中获取的值
     */
    public Object randomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 弹出变量中的元素
     * @param key 键
     * @return 弹出集合Set中的值
     */
    public Object pop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    /**
     * 获取变量中值的长度
     * @param key 键
     * @return 集合Set的大小
     */
    public Long size(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 根据value从一个set中查询,是否存在
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public Boolean sHasKey(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 转移源Set的元素值到目的集合Set。
     * @param key     键
     * @param value   元素对象
     * @param destKey 元素对象
     * @return Ture 移动成功 False移动失败
     */
    public Boolean move(String key, String value, String destKey) {
        return redisTemplate.opsForSet().move(key, value, destKey);
    }

    /**
     * 批量移除set缓存中元素
     * @param key    键
     * @param values 值
     */
    public void remove(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 通过给定的key求2个set变量的差值
     * @param key     键
     * @param destKey 键
     * @return Set key中有而Set destKey中没有的新Set
     */
    public Set<String> difference(String key, String destKey) {
        return redisTemplate.opsForSet().difference(key, destKey);
    }

    //- - - - - - - - - - - - - - - - - - - - -  hash类型 - - - - - - - - - - - - - - - - - - - -

    /**
     * 加入缓存
     * @param key 键
     * @param map 键
     */
    public void add(String key, Map<String, String> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 获取 key 下的 所有  hashkey 和 value
     * @param key 键
     * @return Key-Value 对应Map
     */
    public Map<Object, Object> getHashEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 验证指定 key 下 有没有指定的 hashkey
     * @param key Hash表
     * @param hashKey 表中一个值
     * @return True存在 False不存在
     */
    public boolean hashKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 获取指定key的值string
     * @param key  Hash键
     * @param hashKey HashKey键
     * @return
     */
    public String getMapString(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey).toString();
    }

    /**
     * 获取指定的值Int
     * @param key  Hash键
     * @param hashKey HashKey键
     * @return
     */
    public Integer getMapInt(String key, String hashKey) {
        return (Integer) redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 删除指定 hash 的 HashKey
     * @param key hash的键
     * @param hashKeys hash表中一个hashKey
     * @return 删除成功的 数量
     */
    public Long delete(String key, String... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * 给指定 hash 的 hashkey 做增减操作
     * @param key hash表的key
     * @param hashKey hash表的key中的一个hashKey
     * @param number 增减多少次
     * @return 自增后的值（Long）
     */
    public Long increment(String key, String hashKey, long number) {
        return redisTemplate.opsForHash().increment(key, hashKey, number);
    }

    /**
     * 给指定 hash 的 hashkey 做增减操作
     * @param key hash表的key
     * @param hashKey hash表的key中的一个hashKey
     * @param number 增减多少次
     * @return 自增后的值（Double）
     */
    public Double increment(String key, String hashKey, Double number) {
        return redisTemplate.opsForHash().increment(key, hashKey, number);
    }

    /**
     * 获取 key 下的 所有 hashkey 字段
     * @param key hash表的key
     * @return 所有hashKey组成的Set
     */
    public Set<Object> hashKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取指定 hash 下面的 键值对 数量
     * @param key hash表的key
     * @return hash表的key的大小
     */
    public Long hashSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    //- - - - - - - - - - - - - - - - - - - - -  list类型 - - - - - - - - - - - - - - - - - - - -

    /**
     * 在变量左边添加元素值
     * @param key list表的key
     * @param value 待添加的值
     */
    public void leftPush(String key, String value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 获取集合指定位置的值。
     * @param key list表的key
     * @param index 所在位置索引
     * @return 获取的值
     */
    public Object index(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 获取指定区间的值。
     * @param key list表的key
     * @param start 起点
     * @param end 终点
     * @return 获取的值的 List 集合
     */
    public List<String> range(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 把 value 放到指定集合 key 的第一个出现 pivot 的前面，
     * 如果中间参数值存在的话。
     * @param key list表的key
     * @param pivot 第一次出现的值
     * @param value 待放置的值
     */
    public void leftPush(String key, String pivot, String value) {
        redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    /**
     * 向 List 左边批量添加参数元素。
     * @param key list表的key
     * @param values 待放置的值
     */
    public void leftPushAll(String key, String... values) {
//        redisTemplate.opsForList().leftPushAll(key,"w","x","y");
        redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 向 List 集合最右边添加元素。
     * @param key list表的key
     * @param value 待放置的元素
     */
    public void leftPushAll(String key, String value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 向右边批量添加参数元素。
     * @param key list表的key
     * @param values 待放置的批量元素
     */
    public void rightPushAll(String key, String... values) {
        //redisTemplate.opsForList().leftPushAll(key,"w","x","y");
        redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 向已存在的集合中添加元素，不存在不添加
     * @param key list表的key
     * @param value 值
     */
    public void rightPushIfPresent(String key, String value) {
        redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    /**
     * 获取已存在的集合 List 的大小。
     * @param key list表的key
     * @return 大小size
     */
    public long listLength(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 移除集合中的左边第一个元素并获取。
     * @param key list表的key
     * @return 弹出的值
     */
    public Object leftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 移除集合中左边的元素在等待的时间里，如果超过等待的时间仍没有元素则退出。
     * @param key list表的key
     * @return 弹出的值
     */
    public Object leftPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().leftPop(key, timeout, unit);
    }

    /**
     * 移除集合中右边的元素并获取
     * @param key list表的key
     * @return 弹出的值
     */
    public Object rightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 移除集合中右边的元素在等待的时间里，如果超过等待的时间仍没有元素则退出。
     * @param key list表的key
     * @return
     */
    public Object rightPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPop(key, timeout, unit);
    }
}
