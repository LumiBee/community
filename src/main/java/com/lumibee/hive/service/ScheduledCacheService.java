package com.lumibee.hive.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 定时缓存清理服务
 * 定期清理过期或无效的缓存
 */
@Service
public class ScheduledCacheService {

    @Autowired
    private CacheService cacheService;

    /**
     * 每天凌晨2点清理所有缓存
     * 确保缓存数据不会过时太久
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyCacheCleanup() {
        
        try {
            cacheService.clearAllCaches();
        } catch (Exception e) {
            System.err.println("每日缓存清理任务失败: " + e.getMessage());
        }
        
    }

    /**
     * 每小时清理文章相关缓存
     * 确保首页和个人资料页的文章数据相对新鲜
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void hourlyArticleCacheCleanup() {
        
        try {
            cacheService.clearArticleRelatedCaches();
        } catch (Exception e) {
            System.err.println("每小时文章缓存清理任务失败: " + e.getMessage());
        }
        
    }

    /**
     * 每30分钟清理精选文章缓存
     * 确保精选文章列表保持更新
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void featuredArticlesCacheCleanup() {
        
        try {
            cacheService.clearArticleRelatedCaches();
        } catch (Exception e) {
            System.err.println("精选文章缓存清理失败: " + e.getMessage());
        }
        
    }
}
