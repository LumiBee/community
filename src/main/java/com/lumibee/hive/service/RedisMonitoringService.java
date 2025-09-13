package com.lumibee.hive.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.lumibee.hive.config.CacheMonitoringConfig;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RedisMonitoringService {
    
    @Autowired
    private CacheMonitoringConfig monitoringConfig;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 监控缓存操作
    public <T> T monitorCacheOperation(String operation, Supplier<T> operationSupplier) {
        Timer.Sample sample = monitoringConfig.startTimer();
        try {
            T result = operationSupplier.get();
            if (result != null) {
                monitoringConfig.recordCacheHit();
            } else {
                monitoringConfig.recordCacheMiss();
            }
            return result;
        } finally {
            monitoringConfig.recordTimer(sample);
        }
    }
    
    // 获取缓存统计信息
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取缓存命中率
        double hitRate = getCacheHitRate();
        stats.put("hitRate", hitRate);
        
        // 获取Redis信息
        Map<String, String> redisInfo = getRedisInfo();
        stats.put("redisInfo", redisInfo);
        
        // 获取缓存容量信息
        Map<String, Object> capacityInfo = getCapacityInfo();
        stats.put("capacityInfo", capacityInfo);
        
        // 获取操作统计
        Map<String, Object> operationStats = getOperationStats();
        stats.put("operationStats", operationStats);
        
        return stats;
    }
    
    // 获取Redis详细信息
    private Map<String, String> getRedisInfo() {
        Map<String, String> info = new HashMap<>();
        
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties redisInfo = connection.info();
            
            // 提取关键信息
            info.put("version", redisInfo.getProperty("redis_version"));
            info.put("usedMemory", redisInfo.getProperty("used_memory"));
            info.put("maxMemory", redisInfo.getProperty("maxmemory"));
            info.put("connectedClients", redisInfo.getProperty("connected_clients"));
            info.put("totalCommandsProcessed", redisInfo.getProperty("total_commands_processed"));
            info.put("keyspaceHits", redisInfo.getProperty("keyspace_hits"));
            info.put("keyspaceMisses", redisInfo.getProperty("keyspace_misses"));
            info.put("uptimeInSeconds", redisInfo.getProperty("uptime_in_seconds"));
            
        } catch (Exception e) {
            log.error("获取Redis信息失败", e);
            info.put("error", e.getMessage());
        }
        
        return info;
    }
    
    // 获取缓存容量信息
    private Map<String, Object> getCapacityInfo() {
        Map<String, Object> capacity = new HashMap<>();
        
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties info = connection.info("memory");
            
            long usedMemory = Long.parseLong(info.getProperty("used_memory", "0"));
            long maxMemory = Long.parseLong(info.getProperty("maxmemory", "0"));
            
            capacity.put("usedMemory", usedMemory);
            capacity.put("maxMemory", maxMemory);
            capacity.put("memoryUsagePercent", maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0);
            
            // 获取键数量
            Set<String> keys = redisTemplate.keys("hive::*");
            capacity.put("keyCount", keys != null ? keys.size() : 0);
            
        } catch (Exception e) {
            log.error("获取缓存容量信息失败", e);
        }
        
        return capacity;
    }
    
    // 获取操作统计
    private Map<String, Object> getOperationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 从Micrometer获取指标
        Counter hits = monitoringConfig.getCacheHits();
        Counter misses = monitoringConfig.getCacheMisses();
        Timer timer = monitoringConfig.getCacheOperationTimer();
        
        stats.put("totalHits", hits.count());
        stats.put("totalMisses", misses.count());
        stats.put("averageOperationTime", timer.mean(TimeUnit.MILLISECONDS));
        stats.put("maxOperationTime", timer.max(TimeUnit.MILLISECONDS));
        stats.put("operationCount", timer.count());
        
        return stats;
    }
    
    private double getCacheHitRate() {
        // 从Micrometer获取指标
        Counter hits = monitoringConfig.getCacheHits();
        Counter misses = monitoringConfig.getCacheMisses();
        
        double totalHits = hits.count();
        double totalMisses = misses.count();
        
        if (totalHits + totalMisses == 0) {
            return 0.0;
        }
        
        return totalHits / (totalHits + totalMisses);
    }
}