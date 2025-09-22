package com.lumibee.hive.service.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 消息幂等性检查服务
 * 
 * 基于 Redis 实现消息幂等性检查，防止重复处理
 */
@Slf4j
@Service
public class MessageIdempotencyService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 幂等性检查键前缀
     */
    private static final String IDEMPOTENCY_KEY_PREFIX = "message:idempotency:";

    /**
     * 幂等性检查过期时间（24小时）
     */
    private static final long IDEMPOTENCY_EXPIRE_HOURS = 24;

    /**
     * 检查并设置消息为已处理状态
     * 
     * @param businessId 业务唯一ID
     * @return true 如果消息未被处理过，false 如果消息已被处理过
     */
    public boolean checkAndSetProcessed(String businessId) {
        if (businessId == null || businessId.trim().isEmpty()) {
            log.warn("业务ID为空，跳过幂等性检查");
            return true;
        }

        String key = IDEMPOTENCY_KEY_PREFIX + businessId;
        
        try {
            // 使用 SETNX 命令，如果键不存在则设置，如果存在则返回 false
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "processed");
            
            if (Boolean.TRUE.equals(success)) {
                // 设置过期时间
                redisTemplate.expire(key, Duration.ofHours(IDEMPOTENCY_EXPIRE_HOURS));
                log.debug("消息幂等性检查通过: businessId={}", businessId);
                return true;
            } else {
                log.debug("消息幂等性检查失败，消息已处理: businessId={}", businessId);
                return false;
            }
        } catch (Exception e) {
            log.error("消息幂等性检查异常: businessId={}", businessId, e);
            // 异常情况下，为了安全起见，允许处理消息
            return true;
        }
    }

    /**
     * 检查消息是否已被处理
     * 
     * @param businessId 业务唯一ID
     * @return true 如果消息已被处理过，false 如果消息未被处理过
     */
    public boolean isProcessed(String businessId) {
        if (businessId == null || businessId.trim().isEmpty()) {
            return false;
        }

        String key = IDEMPOTENCY_KEY_PREFIX + businessId;
        
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("检查消息处理状态异常: businessId={}", businessId, e);
            return false;
        }
    }

    /**
     * 清除消息处理状态
     * 
     * @param businessId 业务唯一ID
     */
    public void clearProcessed(String businessId) {
        if (businessId == null || businessId.trim().isEmpty()) {
            return;
        }

        String key = IDEMPOTENCY_KEY_PREFIX + businessId;
        
        try {
            redisTemplate.delete(key);
            log.debug("清除消息处理状态: businessId={}", businessId);
        } catch (Exception e) {
            log.error("清除消息处理状态异常: businessId={}", businessId, e);
        }
    }

    /**
     * 批量清除消息处理状态
     * 
     * @param businessIds 业务唯一ID列表
     */
    public void clearProcessedBatch(java.util.List<String> businessIds) {
        if (businessIds == null || businessIds.isEmpty()) {
            return;
        }

        try {
            java.util.List<String> keys = businessIds.stream()
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .map(id -> IDEMPOTENCY_KEY_PREFIX + id)
                    .collect(java.util.stream.Collectors.toList());
            
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量清除消息处理状态: count={}", keys.size());
            }
        } catch (Exception e) {
            log.error("批量清除消息处理状态异常: businessIds={}", businessIds, e);
        }
    }

    /**
     * 获取消息处理状态信息
     * 
     * @param businessId 业务唯一ID
     * @return 消息处理状态信息
     */
    public MessageProcessStatus getProcessStatus(String businessId) {
        if (businessId == null || businessId.trim().isEmpty()) {
            return new MessageProcessStatus(false, null, null);
        }

        String key = IDEMPOTENCY_KEY_PREFIX + businessId;
        
        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                return new MessageProcessStatus(true, ttl, null);
            } else {
                return new MessageProcessStatus(false, null, null);
            }
        } catch (Exception e) {
            log.error("获取消息处理状态异常: businessId={}", businessId, e);
            return new MessageProcessStatus(false, null, e.getMessage());
        }
    }

    /**
     * 消息处理状态信息
     */
    public static class MessageProcessStatus {
        private final boolean processed;
        private final Long ttlSeconds;
        private final String error;

        public MessageProcessStatus(boolean processed, Long ttlSeconds, String error) {
            this.processed = processed;
            this.ttlSeconds = ttlSeconds;
            this.error = error;
        }

        public boolean isProcessed() {
            return processed;
        }

        public Long getTtlSeconds() {
            return ttlSeconds;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return "MessageProcessStatus{" +
                    "processed=" + processed +
                    ", ttlSeconds=" + ttlSeconds +
                    ", error='" + error + '\'' +
                    '}';
        }
    }
}
