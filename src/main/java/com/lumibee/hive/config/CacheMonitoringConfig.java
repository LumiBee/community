package com.lumibee.hive.config;

import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Configuration
@EnableScheduling
@Log4j2
@Data
public class CacheMonitoringConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存命中率指标
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Timer cacheOperationTimer;

    public CacheMonitoringConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化指标
        this.cacheHits = Counter.builder("cache.hits")
                .description("缓存命中次数")
                .tag("type", "redis")
                .register(meterRegistry);

        this.cacheMisses = Counter.builder("cache.misses")
                .description("缓存未命中次数")
                .tag("type", "redis")
                .register(meterRegistry);

        this.cacheOperationTimer = Timer.builder("cache.operation.duration")
                .description("缓存操作耗时")
                .tag("type", "redis")
                .register(meterRegistry);
    }

    // 记录缓存命中
    public void recordCacheHit() {
        cacheHits.increment();
    }

    // 记录缓存未命中
    public void recordCacheMiss() {
        cacheMisses.increment();
    }

    // 记录缓存操作耗时
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
    public void recordTimer(Timer.Sample sample) {
        sample.stop(cacheOperationTimer);
    }

    // Redis 健康检查
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void checkRedisHealth() {
        try {
            redisTemplate.opsForValue().get("health_check");
            // 记录健康状态
            meterRegistry.gauge("redis.health", 1);
        } catch (Exception e) {
            meterRegistry.gauge("redis.health", 0);
            log.error("Redis 健康检查失败: {}", e.getMessage(), e);
        }
    }

    // 缓存容量监控
    @Scheduled(fixedRate = 60000) // 1分钟检查一次
    public void monitorCacheCapacity() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties info = connection.info("memory");
            String usedMemory = info.getProperty("used_memory");
            String maxMemory = info.getProperty("maxmemory");

            if (usedMemory != null) {
                meterRegistry.gauge("redis.memory.used", Long.parseLong(usedMemory));
            }
            if (maxMemory != null && !maxMemory.equals("0")) {
                meterRegistry.gauge("redis.memory.max", Long.parseLong(maxMemory));
            }

            // 监控缓存键数量
            Set<String> keys = redisTemplate.keys("hive::*");
            meterRegistry.gauge("cache.keys.count", keys != null ? keys.size() : 0);

        } catch (Exception e) {
            log.error("缓存容量监控失败", e);
        }
    }
    
    // 缓存性能监控
    @Scheduled(fixedRate = 300000) // 5分钟检查一次
    public void monitorCachePerformance() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties info = connection.info("stats");
            
            // 监控Redis命令统计
            String totalCommands = info.getProperty("total_commands_processed");
            String keyspaceHits = info.getProperty("keyspace_hits");
            String keyspaceMisses = info.getProperty("keyspace_misses");
            
            if (totalCommands != null) {
                meterRegistry.gauge("redis.commands.total", Long.parseLong(totalCommands));
            }
            if (keyspaceHits != null) {
                meterRegistry.gauge("redis.keyspace.hits", Long.parseLong(keyspaceHits));
            }
            if (keyspaceMisses != null) {
                meterRegistry.gauge("redis.keyspace.misses", Long.parseLong(keyspaceMisses));
            }
            
        } catch (Exception e) {
            log.error("缓存性能监控失败", e);
        }
    }

}
