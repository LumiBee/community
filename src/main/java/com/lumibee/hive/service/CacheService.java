package com.lumibee.hive.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lumibee.hive.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.lumibee.hive.constant.CacheNames;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Tag;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private CacheMonitoringService cacheMonitoringService;

    @Value("${spring.cache.redis.key-prefix:}")
    private String keyPrefix;

    public void clearAllArticleListCaches() {
        clearCachesByPattern(keyPrefix + CacheNames.HOMEPAGE_ARTICLES + "::*");
        clearCachesByPattern(keyPrefix + CacheNames.ALL_ARTICLES + "::*");
        clearCachesByPattern(keyPrefix + CacheNames.TAG_ARTICLES + "::*");
        clearCachesByPattern(keyPrefix + CacheNames.USER_ARTICLES + "::*");
        clearCachesByPattern(keyPrefix + CacheNames.SEARCH_ARTICLES + "::*");
        clearCachesByPattern(keyPrefix + CacheNames.POPULAR_ARTICLES + "::*");
        clearCachesByPattern(keyPrefix + CacheNames.FEATURED_ARTICLES + "::*");
        clearCachesByPattern(keyPrefix + CacheNames.PORTFOLIO_ARTICLES + "::*");
    }

    public void clearAllTagListCaches() {
        clearCachesByPattern(keyPrefix + CacheNames.ALL_TAGS + "::*");
        clearCachesByPattern(keyPrefix + CacheNames.POPULAR_TAGS + "::*");
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

        // 清除相关标签的文章列表缓存 （如果有）
        List<Tag> tags = tagMapper.selectTagsByArticleId(article.getArticleId());
        for (Tag tag : tags) {
            clearTagArticleCaches(tag.getSlug());
        }

        // 5. 清除作品集相关缓存（如果有）
        if (article.getPortfolioId() != null) {
            clearPortfolioArticleCaches(article.getPortfolioId());
        }
    }

    // 监控缓存获取操作
    public <T> T get(String key, Class<T> type) {
        return cacheMonitoringService.monitorCacheOperation("get", () -> {
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
        cacheMonitoringService.monitorCacheOperation("set", () -> {
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
        clearCachesByPattern(keyPrefix + CacheNames.USER_PROFILE + "::" + name);
    }

    /**
     * 清除制定用户粉丝/关注列表缓存
     */
    public void clearUserStatusCaches(Long userId) {
        clearCachesByPattern(keyPrefix + CacheNames.USER_STATUS + "::" + userId);
    }
    
    /**
     * 清除特定标签的文章列表缓存
     */
    public void clearTagArticleCaches(String tagSlug) {
        clearCachesByPattern(keyPrefix + CacheNames.TAG_ARTICLES + "::" + tagSlug);
    }
    
    /**
     * 清除特定用户的文章列表缓存
     */
    public void clearUserArticleCaches(Long userId) {
        clearCachesByPattern(keyPrefix + CacheNames.USER_ARTICLES + "::" + userId);
    }
    
    /**
     * 清除首页文章缓存
     */
    public void clearHomepageArticleCaches() {
        clearCachesByPattern(keyPrefix + CacheNames.HOMEPAGE_ARTICLES + "::*");
    }
    
    /**
     * 清除文章详情缓存
     */
    public void clearArticleDetailCaches(String slug) {
        clearCachesByPattern(keyPrefix + CacheNames.ARTICLE_DETAIL + "::" + slug);
    }
    
    /**
     * 清除作品集文章缓存
     */
    public void clearPortfolioArticleCaches(Integer portfolioId) {
        clearCachesByPattern(keyPrefix + CacheNames.PORTFOLIO_ARTICLES + "::" + portfolioId);
    }

    /**
     * 清除用户关注关系缓存
     * @param userId 被关注者ID
     * @param followerId 关注者ID
     */
    public void clearUserFollowCaches(Long userId, Long followerId) {
        clearCachesByPattern(keyPrefix + CacheNames.USER_FOLLOW + "::" + userId + "::" + followerId);
    }

    /**
     * 清除用户收藏相关缓存
     * @param userId 用户ID
     */
    public void clearUserFavoritesCaches(Long userId) {
        clearCachesByPattern(keyPrefix + CacheNames.FAVORITES_USER + "::" + userId);
    }

    /**
     * 清除作品集详情缓存
     * @param portfolioId 作品集ID
     */
    public void clearPortfolioDetailCaches(Integer portfolioId) {
        clearCachesByPattern(keyPrefix + CacheNames.PORTFOLIO_DETAIL + "::" + portfolioId);
    }

    private void clearCachesByPattern(String pattern) {
        log.info("清除缓存数据, pattern={}", pattern);
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {

            ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(1000).build();

            Cursor<byte[]> cursor = connection.scan(scanOptions);
            Set<String> keysToDelete = new HashSet<>();
            while (cursor.hasNext()) {
                // 找到的 key 是 byte[] 数组，需要转换为 String
                keysToDelete.add(new String(cursor.next()));
            }

            if (!keysToDelete.isEmpty()) {
                log.info("发现了 {} 个缓存数据", keysToDelete.size());
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
