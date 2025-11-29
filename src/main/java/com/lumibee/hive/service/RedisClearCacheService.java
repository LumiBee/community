package com.lumibee.hive.service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.lumibee.hive.mapper.TagMapper;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Tag;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RedisClearCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private RedisMonitoringService redisMonitoringService;

    @Value("${spring.cache.redis.key-prefix:}")
    private String keyPrefix;

    public void clearAllArticleListCaches() {
        // 使用正确的缓存键模式进行清理
        clearCachesByPattern(keyPrefix + "articles::list::homepage::*");
        clearCachesByPattern(keyPrefix + "articles::list::all::*");
        clearCachesByPattern(keyPrefix + "articles::list::tag::*");
        clearCachesByPattern(keyPrefix + "articles::list::user::*");
        clearCachesByPattern(keyPrefix + "articles::list::search::*");
        clearCachesByPattern(keyPrefix + "articles::list::popular::*");
        clearCachesByPattern(keyPrefix + "articles::list::featured::*");
        clearCachesByPattern(keyPrefix + "articles::list::portfolio::*");
    }

    public void clearAllTagListCaches() {
        clearCachesByPattern(keyPrefix + "tags::list::all::*");
        clearCachesByPattern(keyPrefix + "tags::list::popular::*");
    }
    
    /**
     * 清除文章标签列表缓存
     * @param articleId 文章ID
     */
    public void clearArticleTagsCaches(Integer articleId) {
        clearCachesByPattern(keyPrefix + "tags::list::article::" + articleId);
    }
    
    /**
     * 清除标签详情缓存
     * @param tagSlug 标签slug
     */
    public void clearTagDetailCaches(String tagSlug) {
        clearCachesByPattern(keyPrefix + "tags::detail::" + tagSlug);
    }

    public void clearUserRelatedCaches(Long userId, String userName) {
        // 清理用户文章列表缓存
        clearUserArticleCaches(userId);

        // 清理用户相关缓存
        clearUserProfileCaches(userName);
        clearUserStatusCaches(userId);

        // 清除所有文章列表缓存
        clearAllArticleListCaches();

        // 清除所有标签列表缓存
        clearAllTagListCaches();
    }

    public void clearArticleRelatedCaches(Article article) {
        // 清除首页缓存
        clearHomepageArticleCaches();

        // 清除文章详情缓存
        clearArticleDetailCaches(article.getSlug());

        // 清除作者的个人页面缓存
        clearUserArticleCaches(article.getUserId());

        // 清除文章标签列表缓存
        clearArticleTagsCaches(article.getArticleId());

        // 清除相关标签的文章列表缓存 （如果有）
        List<Tag> tags = tagMapper.selectTagsByArticleId(article.getArticleId());
        for (Tag tag : tags) {
            clearTagArticleCaches(tag.getSlug());
            // 清除标签详情缓存（因为文章数量可能发生变化）
            clearTagDetailCaches(tag.getSlug());
        }

        // 5. 清除作品集相关缓存（如果有）
        if (article.getPortfolioId() != null) {
            clearPortfolioArticleCaches(article.getPortfolioId());
        }
    }

    // 监控缓存获取操作
    public <T> T get(String key, Class<T> type) {
        return redisMonitoringService.monitorCacheOperation("get", () -> {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                return value != null ? (T) value : null;
            } catch (Exception e) {
                log.error("Redis get 操作失败: {}", e.getMessage(), e);
                return null;
            }
        });
    }

    // 监控缓存设置操作
    public void set(String key, Object value, Duration ttl) {
        redisMonitoringService.monitorCacheOperation("set", () -> {
            try {
                redisTemplate.opsForValue().set(key, value, ttl);
                return true;
            } catch (Exception e) {
                log.error("设置缓存失败: {}", key, e);
                return false;
            }
        });
    }

    /**
     * 清除指定用户个人中心缓存
     */
    public void clearUserProfileCaches(String name) {
        clearCachesByPattern(keyPrefix + "users::profile::" + name);
    }

    /**
     * 清除制定用户粉丝/关注列表缓存
     */
    public void clearUserStatusCaches(Long userId) {
        clearCachesByPattern(keyPrefix + "users::count::*::" + userId);
    }
    
    /**
     * 清除特定标签的文章列表缓存
     */
    public void clearTagArticleCaches(String tagSlug) {
        clearCachesByPattern(keyPrefix + "articles::list::tag::" + tagSlug);
    }
    
    /**
     * 清除特定用户的文章列表缓存
     */
    public void clearUserArticleCaches(Long userId) {
        clearCachesByPattern(keyPrefix + "articles::list::user::published::" + userId + "::*");
    }

    /**
     * 清除特定用户的草稿文章列表缓存
     */
    public void clearUserDraftCaches(Long userId) {
        clearCachesByPattern(keyPrefix + "articles::list::user::draft::" + userId + "::*");
    }
    
    /**
     * 清除首页文章缓存
     */
    public void clearHomepageArticleCaches() {
        log.info("开始清除首页文章缓存");
        clearCachesByPattern(keyPrefix + "articles::list::homepage::*");
        log.info("首页文章缓存清除完成");
    }
    
    /**
     * 清除文章详情缓存
     */
    public void clearArticleDetailCaches(String slug) {
        clearCachesByPattern(keyPrefix + "articles::detail::" + slug);
    }
    
    /**
     * 清除作品集文章缓存
     */
    public void clearPortfolioArticleCaches(Integer portfolioId) {
        clearCachesByPattern(keyPrefix + "articles::list::portfolio::" + portfolioId);
    }

    /**
     * 清除用户关注关系缓存
     * @param userId 被关注者ID
     * @param followerId 关注者ID
     */
    public void clearUserFollowCaches(Long userId, Long followerId) {
        clearCachesByPattern(keyPrefix + "users::follow::" + userId + "::" + followerId);
    }

    /**
     * 清除用户收藏相关缓存
     * @param userId 用户ID
     */
    public void clearUserFavoritesCaches(Long userId) {
        clearCachesByPattern(keyPrefix + "favorites::list::user::" + userId);
    }

    /**
     * 清除作品集详情缓存
     * @param portfolioId 作品集ID
     */
    public void clearPortfolioDetailCaches(Integer portfolioId) {
        clearCachesByPattern(keyPrefix + "portfolios::detail::" + portfolioId);
    }
    
    /**
     * 手动清除所有缓存（用于调试和紧急情况）
     */
    public void clearAllCaches() {
        log.info("开始清除所有缓存");
        try {
            // 方法1：清除所有hive::开头的缓存
            Set<String> allHiveKeys = redisTemplate.keys("hive::*");
            if (allHiveKeys != null && !allHiveKeys.isEmpty()) {
                log.info("发现 {} 个hive缓存键", allHiveKeys.size());
                redisTemplate.unlink(allHiveKeys);
                log.info("已删除所有hive缓存");
            } else {
                log.info("没有发现hive缓存键");
            }
            
            // 方法2：按类型清除（备用方案）
            clearAllArticleListCaches();
            clearAllTagListCaches();
            
            log.info("所有缓存清除完成");
        } catch (Exception e) {
            log.error("清除所有缓存时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void clearCachesByPattern(String pattern) {
        log.info("清除缓存数据, pattern={}", pattern);
        try {
            // 使用RedisTemplate的keys方法，它会自动处理序列化
            Set<String> keysToDelete = redisTemplate.keys(pattern);
            
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                log.info("发现了 {} 个缓存数据", keysToDelete.size());
                // 使用unlink进行异步删除，性能更好
                redisTemplate.unlink(keysToDelete);
                log.info("已删除缓存数据");
            } else {
                log.info("没有找到任何缓存数据");
            }
        } catch (Exception e) {
            log.error("清除缓存数据时发生错误: {}", e.getMessage(), e);
        }
    }

}
