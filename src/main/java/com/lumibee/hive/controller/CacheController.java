package com.lumibee.hive.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.service.CacheService;

/**
 * 缓存管理控制器
 * 提供手动清理缓存的API接口
 * 仅管理员可访问
 */
@RestController
@RequestMapping("/api/admin/cache")
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
public class CacheController {

    @Autowired
    private CacheService cacheService;

    /**
     * 清理所有缓存
     * DELETE /api/admin/cache/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            cacheService.clearAllCaches();
            response.put("success", true);
            response.put("message", "所有缓存清理成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "缓存清理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 清理文章相关缓存
     * DELETE /api/admin/cache/articles
     */
    @DeleteMapping("/articles")
    public ResponseEntity<Map<String, Object>> clearArticleCaches() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            cacheService.clearArticleRelatedCaches();
            response.put("success", true);
            response.put("message", "文章相关缓存清理成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "缓存清理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 清理特定用户的文章缓存
     * DELETE /api/admin/cache/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> clearUserCaches(Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            cacheService.clearUserArticleCaches(userId);
            response.put("success", true);
            response.put("message", "用户 " + userId + " 的文章缓存清理成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "缓存清理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
