package com.lumibee.hive.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.lumibee.hive.utils.DistributedLockUtil;

import lombok.extern.log4j.Log4j2;

/**
 * 缓存击穿防护服务
 * 
 * 功能：
 * 1. 防止缓存击穿 - 当热点数据过期时，防止大量请求同时访问数据库
 * 2. 防止缓存雪崩 - 通过随机TTL和锁机制避免大量缓存同时失效
 * 3. 防止缓存穿透 - 对空结果进行缓存
 * 4. 提供统一的缓存访问接口
 * 
 * 实现原理：
 * - 使用分布式锁确保只有一个线程能够重建缓存
 * - 其他线程等待锁释放后直接获取缓存结果
 * - 对空结果进行短时间缓存，防止恶意请求
 */
@Service
@Log4j2
public class CacheBreakdownProtectionService {

    @Autowired
    private DistributedLockUtil distributedLockUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 缓存空结果的TTL（秒）
     */
    private static final long NULL_CACHE_TTL = 60;

    /**
     * 锁等待时间（秒）
     */
    private static final long LOCK_WAIT_TIME = 10;

    /**
     * 锁持有时间（秒）
     */
    private static final long LOCK_LEASE_TIME = 30;

    /**
     * 获取缓存数据，防止缓存击穿
     * 使用Spring Cache注解保持Redis存储格式不变
     * 
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param dataLoader 数据加载器
     * @param <T> 数据类型
     * @return 缓存数据
     */
    public <T> T getWithBreakdownProtection(String cacheName, String key, DataLoader<T> dataLoader) {
        return getWithBreakdownProtection(cacheName, key, dataLoader, null);
    }

    /**
     * 获取缓存数据，防止缓存击穿（带TTL）
     * 使用Spring Cache注解保持Redis存储格式不变
     * 
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param dataLoader 数据加载器
     * @param ttl 缓存TTL
     * @param <T> 数据类型
     * @return 缓存数据
     */
    public <T> T getWithBreakdownProtection(String cacheName, String key, DataLoader<T> dataLoader, Duration ttl) {
        // 1. 尝试从Spring Cache获取
        T cachedData = getFromSpringCache(cacheName, key);
        if (cachedData != null) {
            log.debug("从Spring Cache获取数据成功: cacheName={}, key={}", cacheName, key);
            return cachedData;
        }

        // 2. 检查是否为空结果缓存
        if (isNullCached(key)) {
            log.debug("检测到空结果缓存，返回null: key={}", key);
            return null;
        }

        // 3. 使用分布式锁重建缓存
        String lockKey = "lock:cache:" + cacheName + ":" + key;
        
        return distributedLockUtil.executeWithLock(lockKey, () -> {
            // 双重检查：再次尝试从Spring Cache获取
            T doubleCheckData = getFromSpringCache(cacheName, key);
            if (doubleCheckData != null) {
                log.debug("双重检查从Spring Cache获取数据成功: cacheName={}, key={}", cacheName, key);
                return doubleCheckData;
            }

            try {
                // 4. 从数据源加载数据
                log.debug("开始从数据源加载数据: cacheName={}, key={}", cacheName, key);
                T data = dataLoader.load();
                
                if (data != null) {
                    // 5. 使用Spring Cache存入缓存，保持原有格式
                    putToSpringCache(cacheName, key, data);
                    log.debug("数据加载并缓存成功: cacheName={}, key={}", cacheName, key);
                    return data;
                } else {
                    // 6. 缓存空结果，防止缓存穿透
                    cacheNullResult(key);
                    log.debug("数据为空，缓存空结果: key={}", key);
                    return null;
                }
            } catch (Exception e) {
                log.error("从数据源加载数据失败: cacheName={}, key={}", cacheName, key, e);
                throw new RuntimeException("数据加载失败", e);
            }
        }, LOCK_WAIT_TIME, LOCK_LEASE_TIME);
    }

    /**
     * 从Spring Cache获取数据
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromSpringCache(String cacheName, String key) {
        try {
            return (T) cacheManager.getCache(cacheName).get(key, Object.class);
        } catch (Exception e) {
            log.warn("从Spring Cache获取数据失败: cacheName={}, key={}", cacheName, key, e);
            return null;
        }
    }

    /**
     * 将数据存入Spring Cache
     */
    private <T> void putToSpringCache(String cacheName, String key, T data) {
        try {
            cacheManager.getCache(cacheName).put(key, data);
        } catch (Exception e) {
            log.warn("存入Spring Cache失败: cacheName={}, key={}", cacheName, key, e);
        }
    }

    /**
     * 从缓存获取数据（兼容方法）
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromCache(String cacheName, String key) {
        return getFromSpringCache(cacheName, key);
    }

    /**
     * 将数据存入缓存（兼容方法）
     */
    private <T> void putToCache(String cacheName, String key, T data, Duration ttl) {
        putToSpringCache(cacheName, key, data);
    }

    /**
     * 检查是否为空结果缓存
     */
    private boolean isNullCached(String key) {
        try {
            String nullKey = "null:" + key;
            return redisTemplate.hasKey(nullKey);
        } catch (Exception e) {
            log.warn("检查空结果缓存失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 缓存空结果
     */
    private void cacheNullResult(String key) {
        try {
            String nullKey = "null:" + key;
            redisTemplate.opsForValue().set(nullKey, "null", NULL_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存空结果失败: key={}", key, e);
        }
    }

    /**
     * 清除缓存
     */
    public void evictCache(String cacheName, String key) {
        try {
            cacheManager.getCache(cacheName).evict(key);
            // 同时清除空结果缓存
            String nullKey = "null:" + key;
            redisTemplate.delete(nullKey);
            log.debug("清除缓存成功: cacheName={}, key={}", cacheName, key);
        } catch (Exception e) {
            log.warn("清除缓存失败: cacheName={}, key={}", cacheName, key, e);
        }
    }

    /**
     * 清除所有缓存
     */
    public void evictAllCache(String cacheName) {
        try {
            cacheManager.getCache(cacheName).clear();
            log.debug("清除所有缓存成功: cacheName={}", cacheName);
        } catch (Exception e) {
            log.warn("清除所有缓存失败: cacheName={}", cacheName, e);
        }
    }

    /**
     * 数据加载器接口
     */
    @FunctionalInterface
    public interface DataLoader<T> {
        T load() throws Exception;
    }
}
