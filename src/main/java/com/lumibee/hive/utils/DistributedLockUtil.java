package com.lumibee.hive.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * 分布式锁工具类
 * 
 * 功能：
 * 1. 提供分布式锁的获取和释放
 * 2. 防止缓存击穿、缓存雪崩
 * 3. 支持可重入锁
 * 4. 自动锁续期
 * 5. 异常安全处理
 * 
 * 使用场景：
 * - 缓存重建时的并发控制
 * - 防止重复数据加载
 * - 分布式任务执行控制
 */
@Component
@Log4j2
public class DistributedLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 默认锁等待时间（秒）
     */
    private static final long DEFAULT_WAIT_TIME = 10;

    /**
     * 默认锁持有时间（秒）
     */
    private static final long DEFAULT_LEASE_TIME = 30;


    /**
     * 获取分布式锁并执行操作（带超时参数）
     * 
     * @param lockKey 锁的键
     * @param operation 要执行的操作
     * @param waitTime 等待锁的时间（秒）
     * @param leaseTime 锁持有时间（秒）
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> operation, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试获取锁
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            
            if (acquired) {
                log.debug("成功获取分布式锁: {}", lockKey);
                try {
                    // 执行操作
                    return operation.get();
                } finally {
                    // 确保锁被释放
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("成功释放分布式锁: {}", lockKey);
                    }
                }
            } else {
                log.warn("获取分布式锁超时: {}", lockKey);
                throw new RuntimeException("获取分布式锁超时: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", lockKey, e);
            throw new RuntimeException("获取分布式锁被中断: " + lockKey, e);
        } catch (Exception e) {
            log.error("执行分布式锁操作失败: {}", lockKey, e);
            throw new RuntimeException("执行分布式锁操作失败: " + lockKey, e);
        }
    }

    /**
     * 获取分布式锁并执行操作（无返回值，带超时参数）
     * 
     * @param lockKey 锁的键
     * @param operation 要执行的操作
     * @param waitTime 等待锁的时间（秒）
     * @param leaseTime 锁持有时间（秒）
     */
    public void executeWithLock(String lockKey, Runnable operation, long waitTime, long leaseTime) {
        executeWithLock(lockKey, () -> {
            operation.run();
            return null;
        }, waitTime, leaseTime);
    }

    /**
     * 尝试获取锁（非阻塞，带锁持有时间）
     * 
     * @param lockKey 锁的键
     * @param operation 要执行的操作
     * @param leaseTime 锁持有时间（秒）
     * @param <T> 返回值类型
     * @return 操作结果，如果获取锁失败返回null
     */
    public <T> T tryExecuteWithLock(String lockKey, Supplier<T> operation, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试立即获取锁
            boolean acquired = lock.tryLock(0, leaseTime, TimeUnit.SECONDS);
            
            if (acquired) {
                log.debug("成功获取分布式锁: {}", lockKey);
                try {
                    return operation.get();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("成功释放分布式锁: {}", lockKey);
                    }
                }
            } else {
                log.debug("无法获取分布式锁，跳过操作: {}", lockKey);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", lockKey, e);
            return null;
        } catch (Exception e) {
            log.error("执行分布式锁操作失败: {}", lockKey, e);
            return null;
        }
    }

    /**
     * 检查锁是否存在
     * 
     * @param lockKey 锁的键
     * @return 锁是否存在
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * 强制释放锁
     * 
     * @param lockKey 锁的键
     */
    public void forceUnlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            lock.forceUnlock();
            log.warn("强制释放分布式锁: {}", lockKey);
        }
    }

    /**
     * 获取锁的剩余时间
     * 
     * @param lockKey 锁的键
     * @return 剩余时间（毫秒），-1表示锁不存在
     */
    public long getRemainingTime(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.remainTimeToLive();
    }
}
