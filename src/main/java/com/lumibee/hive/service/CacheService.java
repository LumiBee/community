package com.lumibee.hive.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * 缓存管理服务
 * 用于自动清理相关缓存
 */
@Service
public class CacheService {

    @Autowired
    private CacheManager cacheManager;

    // 定义需要清理的缓存名称
    private static final List<String> ARTICLE_RELATED_CACHES = Arrays.asList(
        "homepageArticles",      // 首页文章缓存
        "profileArticles",       // 个人资料页文章缓存
        "featuredArticles",      // 精选文章缓存
        "allArticles",          // 所有文章缓存
        "articleDetails"        // 文章详情缓存
    );

    /**
     * 清理所有文章相关缓存
     * 当用户信息更新时调用
     */
    public void clearArticleRelatedCaches() {
        
        for (String cacheName : ARTICLE_RELATED_CACHES) {
            try {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                } else {
                }
            } catch (Exception e) {
                System.err.println("清理缓存失败 " + cacheName + ": " + e.getMessage());
            }
        }
        
    }

    /**
     * 清理特定用户的文章缓存
     * @param userId 用户ID
     */
    public void clearUserArticleCaches(Long userId) {
        
        // 清理个人资料页文章缓存
        try {
            var profileCache = cacheManager.getCache("profileArticles");
            if (profileCache != null) {
                // 由于profileArticles的key格式是 "userId-pageNum-pageSize"
                // 我们需要清理所有包含该用户ID的缓存
                // 这里简化处理，直接清理整个缓存
                profileCache.clear();
            }
        } catch (Exception e) {
            System.err.println("清理用户个人资料页文章缓存失败: " + e.getMessage());
        }
        
        // 清理首页文章缓存（因为可能包含该用户的文章）
        clearArticleRelatedCaches();
    }

    /**
     * 清理用户名变化相关的缓存
     * 当用户名发生变化时调用，需要清理所有可能包含用户名的缓存
     */
    public void clearUserNameChangeCaches() {
        
        try {
            // 清理所有文章相关缓存（因为文章可能包含作者用户名）
            clearArticleRelatedCaches();
            
            // 清理用户相关缓存
            var userCache = cacheManager.getCache("users");
            if (userCache != null) {
                userCache.clear();
            }
            
            // 清理用户邮箱相关缓存
            var userEmailCache = cacheManager.getCache("usersByEmail");
            if (userEmailCache != null) {
                userEmailCache.clear();
            }
            
            // 清理个人资料页缓存（因为URL可能包含用户名）
            var profileCache = cacheManager.getCache("profileArticles");
            if (profileCache != null) {
                profileCache.clear();
            }
            
        } catch (Exception e) {
            System.err.println("清理用户名变化相关缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清理所有缓存
     * 谨慎使用，仅在必要时调用
     */
    public void clearAllCaches() {
        
        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        } catch (Exception e) {
            System.err.println("清理所有缓存失败: " + e.getMessage());
        }
    }
}
