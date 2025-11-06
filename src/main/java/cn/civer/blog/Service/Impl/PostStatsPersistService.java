package cn.civer.blog.Service.Impl;

import cn.civer.blog.Mapper.PostMapper;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Set;

/**
 * 将 Redis 中累积的浏览/点赞增量异步落盘到数据库。
 * 设计要点：
 * - 前端的增量先写入 Redis 有序集合（member 为 postId，score 为增量累积），不会立即写库。
 * - 定时任务读取每个 member 的 score（增量），写库后从 zset 中减去已落盘的值（使用负增量），
 *   保证并发写入不会丢失（新到的增量会保留在 zset 中）。
 */
@Slf4j
@Component
public class PostStatsPersistService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private PostMapper postMapper;

    /**
     * 每分钟执行一次（可通过 spring 配置覆盖）
     */
    @Scheduled(fixedDelayString = "${blog.stats.flush-ms:60000}")
    public void flushStatsToDb() {
        try {
            flushKey(MessageConstants.REDIS_POST_VIEWS, true);
            flushKey(MessageConstants.REDIS_POST_LIKES, false);
        } catch (Exception e) {
            log.error("落盘文章统计数据时发生异常", e);
        }
    }

    private void flushKey(String key, boolean isViews) {
        Set<String> members = redisUtils.getZSetByRange(key, 0, -1);
        if (members == null || members.isEmpty()) {
            return;
        }
        for (String member : members) {
            try {
                Double scoreD = redisUtils.getZSet(key, member);
                if (scoreD == null) continue;
                long delta = scoreD.longValue();
                if (delta == 0) continue;
                BigInteger postId = new BigInteger(member);
                int updated = 0;
                if (isViews) {
                    updated = postMapper.updateViewsBy(postId, delta);
                } else {
                    updated = postMapper.updateLikesBy(postId, delta);
                }
                if (updated > 0) {
                    // 从 zset 中减去已落盘的增量，保留并发期间新增的值
//                    redisUtils.increZSetScore(key, member, -delta);
                    log.info("已落盘 {}: postId={}, delta={}, rows={}", key, member, delta, updated);
                } else {
                    log.warn("数据库未更新 {}: postId={}, delta={}", key, member, delta);
                }
            } catch (Exception e) {
                log.error("处理 member={} on key={} 时发生错误", new Object[]{member, key, e});
            }
        }
    }
}

