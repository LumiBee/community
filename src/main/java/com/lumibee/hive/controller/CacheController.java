package com.lumibee.hive.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.service.CacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 缓存管理控制器
 * 提供手动清理缓存的API接口
 * 仅管理员可访问
 */
@RestController
@RequestMapping("/api/admin/cache")
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
@Tag(name = "缓存管理", description = "管理缓存相关的 API 接口，需要管理员身份")
public class CacheController {

    @Autowired
    private CacheService cacheService;

    /**
     * 清理所有缓存
     * DELETE /api/admin/cache/all
     */
    @DeleteMapping("/all")
    @Operation(summary = "清理所有缓存", description = "清理系统中的所有缓存")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "缓存清理成功"),
        @ApiResponse(responseCode = "500", description = "缓存清理失败")
    })
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
    @Operation(summary = "清理文章缓存", description = "清理所有文章相关的缓存")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "缓存清理成功"),
        @ApiResponse(responseCode = "500", description = "缓存清理失败")
    })
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
    @Operation(summary = "清理用户缓存", description = "清理指定用户相关的文章缓存")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "缓存清理成功"),
        @ApiResponse(responseCode = "500", description = "缓存清理失败")
    })
    public ResponseEntity<Map<String, Object>> clearUserCaches(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
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
