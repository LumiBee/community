package com.lumibee.hive.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.service.CacheMonitoringService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/monitoring")
@Tag(name = "监控管理", description = "系统监控相关的 API 接口")
public class MonitoringController {

    @Autowired
    private CacheMonitoringService cacheMonitoringService;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计信息", description = "获取缓存的命中率、Redis状态等统计信息")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = cacheMonitoringService.getCacheStats();
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
        Map<String, Object> cacheStats = cacheMonitoringService.getCacheStats();
        summary.put("cache", cacheStats);
        
        // 添加时间戳
        summary.put("timestamp", System.currentTimeMillis());
        summary.put("application", "hive");
        
        return ResponseEntity.ok(summary);
    }
}
