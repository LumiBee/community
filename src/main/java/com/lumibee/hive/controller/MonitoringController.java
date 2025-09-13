package com.lumibee.hive.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.service.DataWarmupService;
import com.lumibee.hive.service.RedisClearCacheService;
import com.lumibee.hive.service.RedisMonitoringService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/monitoring")
@Tag(name = "监控管理", description = "系统监控相关的 API 接口")
public class MonitoringController {

    @Autowired
    private RedisMonitoringService redisMonitoringService;

    @Autowired
    private RedisClearCacheService redisClearCacheService;

    @Autowired
    private DataWarmupService dataWarmupService;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计信息", description = "获取缓存的命中率、Redis状态等统计信息")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = redisMonitoringService.getCacheStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    @Operation(summary = "获取系统健康状态")
    public ResponseEntity<Map<String, Object>> getHealth() {
        HealthComponent health = healthEndpoint.health();
        Map<String, Object> healthMap = new HashMap<>();
        healthMap.put("status", health.getStatus());
        healthMap.put("details", health);
        return ResponseEntity.ok(healthMap);
    }
    
    @GetMapping("/metrics/summary")
    @Operation(summary = "获取监控指标摘要")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // 获取缓存统计
        Map<String, Object> cacheStats = redisMonitoringService.getCacheStats();
        summary.put("cache", cacheStats);
        
        // 添加时间戳
        summary.put("timestamp", System.currentTimeMillis());
        summary.put("application", "hive");
        
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/cache/clear")
    @Operation(summary = "清除所有缓存", description = "手动清除所有缓存，用于调试和紧急情况")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        Map<String, Object> result = new HashMap<>();
        try {
            redisClearCacheService.clearAllCaches();
            result.put("success", true);
            result.put("message", "所有缓存已清除");
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清除缓存失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    @PostMapping("/cache/clear/homepage")
    @Operation(summary = "清除首页缓存", description = "手动清除首页文章缓存")
    public ResponseEntity<Map<String, Object>> clearHomepageCache() {
        Map<String, Object> result = new HashMap<>();
        try {
            redisClearCacheService.clearHomepageArticleCaches();
            result.put("success", true);
            result.put("message", "首页缓存已清除");
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清除首页缓存失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    @PostMapping("/cache/clear-and-warmup")
    @Operation(summary = "清除所有缓存并重新预热", description = "清除所有缓存并重新预热数据，用于调试和紧急情况")
    public ResponseEntity<Map<String, Object>> clearAndWarmupCaches() {
        Map<String, Object> result = new HashMap<>();
        try {
            dataWarmupService.clearAndWarmupAllCaches();
            result.put("success", true);
            result.put("message", "缓存已清除并重新预热完成");
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清除缓存并预热失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
