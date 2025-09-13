package com.lumibee.hive.service;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.model.Article;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CounterSyncService {
    
    @Autowired
    private RedisCounterService redisCounterService;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    private static final Pattern ARTICLE_VIEW_PATTERN = Pattern.compile("^article:view:(\\d+)$");
    private static final Pattern ARTICLE_LIKE_PATTERN = Pattern.compile("^article:like:(\\d+)$");
    private static final Pattern USER_LIKE_PATTERN = Pattern.compile("^user:like:(\\d+)$");
    private static final Pattern USER_FOLLOW_PATTERN = Pattern.compile("^user:follow:(\\d+)$");
    private static final Pattern USER_FANS_PATTERN = Pattern.compile("^user:fans:(\\d+)$");
    
    /**
     * 每5分钟同步一次计数器到数据库
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void syncCountersToDatabase() {
        log.info("开始同步Redis计数器到数据库...");
        
        try {
            Set<String> counterKeys = redisCounterService.getAllCounterKeys();
            if (counterKeys.isEmpty()) {
                log.info("没有找到需要同步的计数器");
                return;
            }
            
            log.info("发现 {} 个计数器需要同步", counterKeys.size());
            
            int syncedCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            
            for (String key : counterKeys) {
                String cleanKey = key.replace("counter:", "");
                int count = redisCounterService.getCount(cleanKey);
                
                SyncResult result = syncCounterToDatabase(cleanKey, count);
                switch (result) {
                    case SUCCESS -> syncedCount++;
                    case FAILED -> failedCount++;
                    case SKIPPED -> skippedCount++;
                }
            }
            
            log.info("计数器同步完成 - 成功: {}, 失败: {}, 跳过: {}, 总计: {}", 
                    syncedCount, failedCount, skippedCount, counterKeys.size());
            
        } catch (Exception e) {
            log.error("同步计数器到数据库失败", e);
        }
    }
    
    /**
     * 根据计数器类型同步到对应数据库表
     */
    @Transactional
    public SyncResult syncCounterToDatabase(String key, int count) {
        try {
            // 文章阅读量
            if (ARTICLE_VIEW_PATTERN.matcher(key).matches()) {
                int articleId = Integer.parseInt(key.split(":")[2]);
                
                // 检查文章是否存在
                Article existingArticle = articleMapper.selectById(articleId);
                if (existingArticle == null) {
                    log.warn("文章 {} 不存在，跳过阅读量同步", articleId);
                    return SyncResult.SKIPPED;
                }
                
                Article article = new Article();
                article.setArticleId(articleId);
                article.setViewCount(count);
                int updated = articleMapper.updateById(article);
                
                if (updated > 0) {
                    log.debug("同步文章 {} 阅读量: {} (原值: {})", articleId, count, existingArticle.getViewCount());
                    return SyncResult.SUCCESS;
                } else {
                    log.warn("更新文章 {} 阅读量失败", articleId);
                    return SyncResult.FAILED;
                }
            }
            
            // 文章点赞数
            if (ARTICLE_LIKE_PATTERN.matcher(key).matches()) {
                int articleId = Integer.parseInt(key.split(":")[2]);
                
                // 检查文章是否存在
                Article existingArticle = articleMapper.selectById(articleId);
                if (existingArticle == null) {
                    log.warn("文章 {} 不存在，跳过点赞数同步", articleId);
                    return SyncResult.SKIPPED;
                }
                
                Article article = new Article();
                article.setArticleId(articleId);
                article.setLikes(count);
                int updated = articleMapper.updateById(article);
                
                if (updated > 0) {
                    log.debug("同步文章 {} 点赞数: {} (原值: {})", articleId, count, existingArticle.getLikes());
                    return SyncResult.SUCCESS;
                } else {
                    log.warn("更新文章 {} 点赞数失败", articleId);
                    return SyncResult.FAILED;
                }
            }
            
            // 其他未知类型的计数器
            log.debug("未知类型计数器 {} 值为 {}，跳过同步", key, count);
            return SyncResult.SKIPPED;
            
        } catch (NumberFormatException e) {
            log.error("解析计数器键 {} 中的ID失败: {}", key, e.getMessage());
            return SyncResult.FAILED;
        } catch (Exception e) {
            log.error("同步计数器 {} 到数据库失败: {}", key, e.getMessage(), e);
            return SyncResult.FAILED;
        }
    }

    /**
     * 同步特定类型的计数器
     */
    public void syncArticleCounters() {
        log.info("开始同步文章相关计数器...");
        
        Set<String> counterKeys = redisCounterService.getAllCounterKeys();
        if (counterKeys.isEmpty()) {
            log.info("没有找到需要同步的文章计数器");
            return;
        }
        
        int syncedCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        
        for (String key : counterKeys) {
            String cleanKey = key.replace("counter:", "");
            int count = redisCounterService.getCount(cleanKey);
            
            if (ARTICLE_VIEW_PATTERN.matcher(cleanKey).matches() || 
                ARTICLE_LIKE_PATTERN.matcher(cleanKey).matches()) {
                
                SyncResult result = syncCounterToDatabase(cleanKey, count);
                switch (result) {
                    case SUCCESS -> syncedCount++;
                    case FAILED -> failedCount++;
                    case SKIPPED -> skippedCount++;
                }
            }
        }
        
        log.info("文章计数器同步完成 - 成功: {}, 失败: {}, 跳过: {}", 
                syncedCount, failedCount, skippedCount);
    }
    
    /**
     * 同步用户相关计数器
     */
    public void syncUserCounters() {
        log.info("开始同步用户相关计数器...");
        
        Set<String> counterKeys = redisCounterService.getAllCounterKeys();
        if (counterKeys.isEmpty()) {
            log.info("没有找到需要同步的用户计数器");
            return;
        }
        
        int syncedCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        
        for (String key : counterKeys) {
            String cleanKey = key.replace("counter:", "");
            int count = redisCounterService.getCount(cleanKey);
            
            if (USER_LIKE_PATTERN.matcher(cleanKey).matches() || 
                USER_FOLLOW_PATTERN.matcher(cleanKey).matches() ||
                USER_FANS_PATTERN.matcher(cleanKey).matches()) {
                
                SyncResult result = syncCounterToDatabase(cleanKey, count);
                switch (result) {
                    case SUCCESS -> syncedCount++;
                    case FAILED -> failedCount++;
                    case SKIPPED -> skippedCount++;
                }
            }
        }
        
        log.info("用户计数器同步完成 - 成功: {}, 失败: {}, 跳过: {}", 
                syncedCount, failedCount, skippedCount);
    }
    
    /**
     * 手动同步所有计数器（用于管理接口）
     */
    public void manualSyncAll() {
        log.info("手动触发同步所有计数器...");
        syncCountersToDatabase();
    }
    
    /**
     * 获取同步统计信息
     */
    public void printSyncStatistics() {
        Set<String> counterKeys = redisCounterService.getAllCounterKeys();
        log.info("当前Redis中的计数器统计:");
        log.info("- 总计数器数量: {}", counterKeys.size());
        
        int articleViewCount = 0;
        int articleLikeCount = 0;
        int userLikeCount = 0;
        int userFollowCount = 0;
        int userFansCount = 0;
        int otherCount = 0;
        
        for (String key : counterKeys) {
            String cleanKey = key.replace("counter:", "");
            if (ARTICLE_VIEW_PATTERN.matcher(cleanKey).matches()) {
                articleViewCount++;
            } else if (ARTICLE_LIKE_PATTERN.matcher(cleanKey).matches()) {
                articleLikeCount++;
            } else if (USER_LIKE_PATTERN.matcher(cleanKey).matches()) {
                userLikeCount++;
            } else if (USER_FOLLOW_PATTERN.matcher(cleanKey).matches()) {
                userFollowCount++;
            } else if (USER_FANS_PATTERN.matcher(cleanKey).matches()) {
                userFansCount++;
            } else {
                otherCount++;
            }
        }
        
        log.info("- 文章阅读量计数器: {}", articleViewCount);
        log.info("- 文章点赞数计数器: {}", articleLikeCount);
        log.info("- 用户点赞数计数器: {}", userLikeCount);
        log.info("- 用户关注数计数器: {}", userFollowCount);
        log.info("- 用户粉丝数计数器: {}", userFansCount);
        log.info("- 其他类型计数器: {}", otherCount);
    }
}

enum SyncResult {
    SUCCESS,    // 同步成功
    FAILED,     // 同步失败
    SKIPPED     // 跳过同步
}
