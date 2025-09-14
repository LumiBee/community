package com.lumibee.hive.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 热门文章服务
 * 
 * 功能：
 * 1. 使用 Redis Sorted Set 实现热门文章排序
 * 2. 支持文章热度分数计算和更新
 * 3. 提供热门文章列表查询
 */
@Slf4j
@Service
public class RedisPopularArticleService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private com.lumibee.hive.mapper.ArticleMapper articleMapper;

    private static final String POPULAR_ARTICLES_KEY = "popular:articles";
    private static final String ARTICLE_VIEW_COUNT_KEY = "article:view:";
    private static final String ARTICLE_LIKE_COUNT_KEY = "article:like:";
    private static final String ARTICLE_COMMENT_COUNT_KEY = "article:comment:";

    /**
     * 更新文章热度分数
     * 
     * @param articleId 文章ID
     * @param viewCount 浏览量
     * @param likeCount 点赞数
     * @param commentCount 评论数
     * @param publishTime 文章发布时间
     */
    public void updateArticlePopularity(Integer articleId, Integer viewCount, Integer likeCount, Integer commentCount, java.time.LocalDateTime publishTime) {
        try {
            // 计算热度分数
            double popularityScore = calculatePopularityScore(viewCount, likeCount, commentCount, publishTime);
            
            // 更新到 Redis Sorted Set
            redisTemplate.opsForZSet().add(POPULAR_ARTICLES_KEY, articleId.toString(), popularityScore);
            
            // 设置过期时间（7天）
            redisTemplate.expire(POPULAR_ARTICLES_KEY, 7, TimeUnit.DAYS);
            
            log.debug("更新文章热度分数: articleId={}, score={}, publishTime={}", articleId, popularityScore, publishTime);
            
        } catch (Exception e) {
            log.error("更新文章热度分数失败: articleId={}", articleId, e);
        }
    }


    /**
     * 增加文章浏览量
     * 
     * @param articleId 文章ID
     */
    public void incrementArticleView(Integer articleId) {
        try {
            // 增加浏览量
            String viewKey = ARTICLE_VIEW_COUNT_KEY + articleId;
            redisTemplate.opsForValue().increment(viewKey);
            redisTemplate.expire(viewKey, 7, TimeUnit.DAYS);
            
        } catch (Exception e) {
            log.error("增加文章浏览量失败: articleId={}", articleId, e);
        }
    }

    /**
     * 增加文章点赞数
     * 
     * @param articleId 文章ID
     */
    public void incrementArticleLike(Integer articleId) {
        try {
            // 增加点赞数
            String likeKey = ARTICLE_LIKE_COUNT_KEY + articleId;
            redisTemplate.opsForValue().increment(likeKey);
            redisTemplate.expire(likeKey, 7, TimeUnit.DAYS);
            
        } catch (Exception e) {
            log.error("增加文章点赞数失败: articleId={}", articleId, e);
        }
    }

    /**
     * 减少文章点赞数
     * 
     * @param articleId 文章ID
     */
    public void decrementArticleLike(Integer articleId) {
        try {
            // 减少点赞数
            String likeKey = ARTICLE_LIKE_COUNT_KEY + articleId;
            redisTemplate.opsForValue().decrement(likeKey);
            redisTemplate.expire(likeKey, 7, TimeUnit.DAYS);

            
        } catch (Exception e) {
            log.error("减少文章点赞数失败: articleId={}", articleId, e);
        }
    }

    /**
     * 增加文章评论数
     * 
     * @param articleId 文章ID
     */
    public void incrementArticleComment(Integer articleId) {
        try {
            // 增加评论数
            String commentKey = ARTICLE_COMMENT_COUNT_KEY + articleId;
            redisTemplate.opsForValue().increment(commentKey);
            redisTemplate.expire(commentKey, 7, TimeUnit.DAYS);
            
        } catch (Exception e) {
            log.error("增加文章评论数失败: articleId={}", articleId, e);
        }
    }

    /**
     * 获取热门文章ID列表
     * 
     * @param limit 限制数量
     * @return 文章ID列表（按热度降序）
     */
    public List<String> getPopularArticleIds(int limit) {
        try {
            // 从 Redis Sorted Set 获取热门文章ID（按分数降序）
            Set<Object> articleIds = redisTemplate.opsForZSet().reverseRange(POPULAR_ARTICLES_KEY, 0, limit - 1);
            
            if (articleIds == null || articleIds.isEmpty()) {
                log.debug("Redis 中没有热门文章数据");
                return List.of();
            }
            
            List<String> result = articleIds.stream()
                    .map(Object::toString)
                    .toList();
            
            log.debug("获取热门文章ID列表: limit={}, count={}", limit, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取热门文章ID列表失败: limit={}", limit, e);
            return List.of();
        }
    }

    /**
     * 从 Redis 获取计数值
     * 
     * @param key Redis 键
     * @return 计数值
     */
    private Integer getRedisCount(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.parseInt(value.toString()) : 0;
        } catch (Exception e) {
            log.debug("获取 Redis 计数值失败: key={}", key, e);
            return 0;
        }
    }

    /**
     * 计算热度分数
     * 
     * @param viewCount 浏览量
     * @param likeCount 点赞数
     * @param commentCount 评论数
     * @param publishTime 文章发布时间
     * @return 热度分数
     */
    private double calculatePopularityScore(Integer viewCount, Integer likeCount, Integer commentCount, java.time.LocalDateTime publishTime) {
        // 基础热度分数计算
        // 浏览量权重: 1.0
        // 点赞数权重: 5.0
        // 评论数权重: 5.0
        double baseScore = (viewCount * 1.0) + (likeCount * 5.0) + (commentCount * 5.0);
        
        // 计算时间衰减因子
        double timeDecayFactor = calculateTimeDecayFactor(publishTime);
        
        // 应用时间衰减
        double finalScore = baseScore * timeDecayFactor;
        
        return finalScore;
    }

    /**
     * 计算时间衰减因子
     * 
     * @param publishTime 文章发布时间
     * @return 时间衰减因子
     */
    private double calculateTimeDecayFactor(java.time.LocalDateTime publishTime) {
        if (publishTime == null) {
            return 1.0; // 如果没有发布时间，不进行衰减
        }
        
        // 计算文章发布到现在的小时数
        long hoursSincePublish = java.time.Duration.between(publishTime, java.time.LocalDateTime.now()).toHours();
        
        // 时间衰减算法：使用对数衰减
        // 公式：decay = 1 / (1 + hours / decayConstant)
        // decayConstant 控制衰减速度，值越大衰减越慢
        double decayConstant = 24.0; // 24小时后衰减到50%
        
        double decayFactor = 1.0 / (1.0 + hoursSincePublish / decayConstant);
        
        // 设置最小衰减因子，避免分数过低
        double minDecayFactor = 0.2; // 最小保持20%的分数
        
        return Math.max(decayFactor, minDecayFactor);
    }

    /**
     * 定时重新计算所有文章的热度分数
     * 每天执行一次
     */
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24小时
    public void recalculateAllArticlePopularity() {
        try {
            log.info("开始重新计算所有文章的热度分数...");
            
            // 获取所有热门文章ID
            Set<Object> allArticleIds = redisTemplate.opsForZSet().range(POPULAR_ARTICLES_KEY, 0, -1);
            
            if (allArticleIds == null || allArticleIds.isEmpty()) {
                log.info("没有找到需要重新计算的文章");
                return;
            }
            
            int processedCount = 0;
            int errorCount = 0;
            
            for (Object articleIdObj : allArticleIds) {
                try {
                    Integer articleId = Integer.parseInt(articleIdObj.toString());
                    
                    // 从 Redis 获取当前数据
                    Integer viewCount = getRedisCount(ARTICLE_VIEW_COUNT_KEY + articleId);
                    Integer likeCount = getRedisCount(ARTICLE_LIKE_COUNT_KEY + articleId);
                    Integer commentCount = getRedisCount(ARTICLE_COMMENT_COUNT_KEY + articleId);
                    
                    // 从数据库获取文章发布时间
                    com.lumibee.hive.model.Article article = articleMapper.selectById(articleId);
                    java.time.LocalDateTime publishTime = article != null ? article.getGmtCreate() : null;
                    
                    // 重新计算热度分数
                    updateArticlePopularity(articleId, viewCount, likeCount, commentCount, publishTime);
                    
                    processedCount++;
                    
                } catch (Exception e) {
                    log.warn("重新计算文章热度分数失败: articleId={}", articleIdObj, e);
                    errorCount++;
                }
            }
            
            log.info("重新计算文章热度分数完成: 处理={}, 错误={}, 总计={}", 
                    processedCount, errorCount, allArticleIds.size());
            
        } catch (Exception e) {
            log.error("定时重新计算文章热度分数失败", e);
        }
    }

}
