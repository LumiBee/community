package com.lumibee.hive.service;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis 计数器服务
 * 提供各种计数器的 Redis 操作，包括文章阅读量、点赞数、用户关注数、粉丝数等
 */
@Service
public class RedisCounterService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String COUNTER_PREFIX = "counter:";
    
    // ==================== 基础操作方法 ====================
    
    /**
     * 增加计数器
     */
    public int increment(String key) {
        String redisKey = COUNTER_PREFIX + key;
        Long result = redisTemplate.opsForValue().increment(redisKey);
        return result != null ? result.intValue() : 0;
    }
    
    /**
     * 增加指定数量
     */
    public int incrementBy(String key, int delta) {
        String redisKey = COUNTER_PREFIX + key;
        Long result = redisTemplate.opsForValue().increment(redisKey, delta);
        return result != null ? result.intValue() : 0;
    }
    
    /**
     * 减少计数器
     */
    public int decrement(String key) {
        String redisKey = COUNTER_PREFIX + key;
        Long result = redisTemplate.opsForValue().decrement(redisKey);
        return result != null ? result.intValue() : 0;
    }
    
    /**
     * 获取计数器值
     */
    public int getCount(String key) {
        String redisKey = COUNTER_PREFIX + key;
        Object value = redisTemplate.opsForValue().get(redisKey);
        return value != null ? Integer.parseInt(value.toString()) : 0;
    }
    
    /**
     * 设置计数器值
     */
    public void setCount(String key, int value) {
        String redisKey = COUNTER_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, value);
    }
    
    /**
     * 检查计数器键是否存在
     */
    public boolean exists(String key) {
        String redisKey = COUNTER_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }
    
    /**
     * 删除计数器
     */
    public void deleteCounter(String key) {
        String redisKey = COUNTER_PREFIX + key;
        redisTemplate.delete(redisKey);
    }
    
    // ==================== 文章阅读量相关 ====================
    
    /**
     * 文章阅读量计数增加
     */
    public int incrementArticleView(Integer articleId) {
        String key = "article:view:" + articleId;
        return increment(key);
    }
    
    /**
     * 获取文章阅读量
     */
    public int getArticleViewCount(Integer articleId) {
        String key = "article:view:" + articleId;
        return getCount(key);
    }
    
    /**
     * 设置文章阅读量
     */
    public void setArticleViewCount(Integer articleId, int count) {
        String key = "article:view:" + articleId;
        setCount(key, count);
    }
    
    /**
     * 检查文章阅读量是否存在
     */
    public boolean existsArticleViewCount(Integer articleId) {
        String key = "article:view:" + articleId;
        return exists(key);
    }
    
    /**
     * 删除文章阅读量计数器
     */
    public void deleteArticleViewCount(Integer articleId) {
        String key = "article:view:" + articleId;
        deleteCounter(key);
    }
    
    // ==================== 文章点赞相关 ====================
    
    /**
     * 文章点赞计数增加
     */
    public int incrementArticleLike(Integer articleId) {
        String key = "article:like:" + articleId;
        return increment(key);
    }
    
    /**
     * 文章点赞计数减少
     */
    public int decrementArticleLike(Integer articleId) {
        String key = "article:like:" + articleId;
        return decrement(key);
    }
    
    /**
     * 获取文章点赞数
     */
    public int getArticleLikeCount(Integer articleId) {
        String key = "article:like:" + articleId;
        return getCount(key);
    }
    
    /**
     * 设置文章点赞数
     */
    public void setArticleLikeCount(Integer articleId, int count) {
        String key = "article:like:" + articleId;
        setCount(key, count);
    }
    
    /**
     * 检查文章点赞数是否存在
     */
    public boolean existsArticleLikeCount(Integer articleId) {
        String key = "article:like:" + articleId;
        return exists(key);
    }
    
    /**
     * 删除文章点赞数计数器
     */
    public void deleteArticleLikeCount(Integer articleId) {
        String key = "article:like:" + articleId;
        deleteCounter(key);
    }
    
    // ==================== 用户关注相关 ====================
    
    /**
     * 用户关注计数增加
     */
    public int incrementUserFollow(Long userId) {
        String key = "user:follow:" + userId;
        return increment(key);
    }
    
    /**
     * 用户关注计数减少
     */
    public int decrementUserFollow(Long userId) {
        String key = "user:follow:" + userId;
        return decrement(key);
    }
    
    /**
     * 获取用户关注数
     */
    public int getUserFollowCount(Long userId) {
        String key = "user:follow:" + userId;
        return getCount(key);
    }
    
    /**
     * 设置用户关注数
     */
    public void setUserFollowCount(Long userId, int count) {
        String key = "user:follow:" + userId;
        setCount(key, count);
    }
    
    /**
     * 检查用户关注数是否存在
     */
    public boolean existsUserFollowCount(Long userId) {
        String key = "user:follow:" + userId;
        return exists(key);
    }
    
    /**
     * 删除用户关注数计数器
     */
    public void deleteUserFollowCount(Long userId) {
        String key = "user:follow:" + userId;
        deleteCounter(key);
    }
    
    // ==================== 用户粉丝相关 ====================
    
    /**
     * 用户粉丝计数增加
     */
    public int incrementUserFans(Long userId) {
        String key = "user:fans:" + userId;
        return increment(key);
    }
    
    /**
     * 用户粉丝计数减少
     */
    public int decrementUserFans(Long userId) {
        String key = "user:fans:" + userId;
        return decrement(key);
    }
    
    /**
     * 获取用户粉丝数
     */
    public int getUserFansCount(Long userId) {
        String key = "user:fans:" + userId;
        return getCount(key);
    }
    
    /**
     * 设置用户粉丝数
     */
    public void setUserFansCount(Long userId, int count) {
        String key = "user:fans:" + userId;
        setCount(key, count);
    }
    
    /**
     * 检查用户粉丝数是否存在
     */
    public boolean existsUserFansCount(Long userId) {
        String key = "user:fans:" + userId;
        return exists(key);
    }
    
    /**
     * 删除用户粉丝数计数器
     */
    public void deleteUserFansCount(Long userId) {
        String key = "user:fans:" + userId;
        deleteCounter(key);
    }
    
    // ==================== 用户点赞相关 ====================
    
    /**
     * 用户点赞计数增加
     */
    public int incrementUserLike(Long userId) {
        String key = "user:like:" + userId;
        return increment(key);
    }
    
    /**
     * 用户点赞计数减少
     */
    public int decrementUserLike(Long userId) {
        String key = "user:like:" + userId;
        return decrement(key);
    }
    
    /**
     * 获取用户点赞数
     */
    public int getUserLikeCount(Long userId) {
        String key = "user:like:" + userId;
        return getCount(key);
    }
    
    /**
     * 设置用户点赞数
     */
    public void setUserLikeCount(Long userId, int count) {
        String key = "user:like:" + userId;
        setCount(key, count);
    }
    
    /**
     * 检查用户点赞数是否存在
     */
    public boolean existsUserLikeCount(Long userId) {
        String key = "user:like:" + userId;
        return exists(key);
    }
    
    /**
     * 删除用户点赞数计数器
     */
    public void deleteUserLikeCount(Long userId) {
        String key = "user:like:" + userId;
        deleteCounter(key);
    }
    
    // ==================== 批量操作方法 ====================
    
    /**
     * 批量获取计数器值
     */
    public Map<String, Integer> getBatchCounts(Set<String> keys) {
        java.util.List<String> redisKeys = keys.stream()
            .map(key -> COUNTER_PREFIX + key)
            .collect(java.util.stream.Collectors.toList());
        
        java.util.List<Object> values = redisTemplate.opsForValue().multiGet(redisKeys);
        
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        int index = 0;
        for (String key : keys) {
            Object value = values != null ? values.get(index) : null;
            result.put(key, value != null ? (Integer) value : 0);
            index++;
        }
        return result;
    }
    
    /**
     * 批量删除计数器
     */
    public void deleteBatchCounters(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        java.util.List<String> redisKeys = keys.stream()
            .map(key -> COUNTER_PREFIX + key)
            .collect(java.util.stream.Collectors.toList());
        
        redisTemplate.delete(redisKeys);
    }
    
    // ==================== 文章收藏相关 ====================
    
    /**
     * 文章被收藏次数增加
     */
    public int incrementArticleFavorite(Integer articleId) {
        String key = "article:favorite:" + articleId;
        return increment(key);
    }
    
    /**
     * 文章被收藏次数减少
     */
    public int decrementArticleFavorite(Integer articleId) {
        String key = "article:favorite:" + articleId;
        return decrement(key);
    }
    
    /**
     * 获取文章被收藏次数
     */
    public int getArticleFavoriteCount(Integer articleId) {
        String key = "article:favorite:" + articleId;
        return getCount(key);
    }
    
    /**
     * 设置文章被收藏次数
     */
    public void setArticleFavoriteCount(Integer articleId, int count) {
        String key = "article:favorite:" + articleId;
        setCount(key, count);
    }
    
    /**
     * 检查文章被收藏次数是否存在
     */
    public boolean existsArticleFavoriteCount(Integer articleId) {
        String key = "article:favorite:" + articleId;
        return exists(key);
    }
    
    /**
     * 删除文章被收藏次数计数器
     */
    public void deleteArticleFavoriteCount(Integer articleId) {
        String key = "article:favorite:" + articleId;
        deleteCounter(key);
    }
    
    // ==================== 收藏夹文章相关 ====================
    
    /**
     * 收藏夹文章数量增加
     */
    public int incrementFavoriteArticle(Long favoriteId) {
        String key = "favorite:article:" + favoriteId;
        return increment(key);
    }
    
    /**
     * 收藏夹文章数量减少
     */
    public int decrementFavoriteArticle(Long favoriteId) {
        String key = "favorite:article:" + favoriteId;
        return decrement(key);
    }
    
    /**
     * 获取收藏夹文章数量
     */
    public int getFavoriteArticleCount(Long favoriteId) {
        String key = "favorite:article:" + favoriteId;
        return getCount(key);
    }
    
    /**
     * 设置收藏夹文章数量
     */
    public void setFavoriteArticleCount(Long favoriteId, int count) {
        String key = "favorite:article:" + favoriteId;
        setCount(key, count);
    }
    
    /**
     * 检查收藏夹文章数量是否存在
     */
    public boolean existsFavoriteArticleCount(Long favoriteId) {
        String key = "favorite:article:" + favoriteId;
        return exists(key);
    }
    
    /**
     * 删除收藏夹文章数量计数器
     */
    public void deleteFavoriteArticleCount(Long favoriteId) {
        String key = "favorite:article:" + favoriteId;
        deleteCounter(key);
    }
    
    // ==================== 用户收藏相关 ====================
    
    /**
     * 用户收藏文章数量增加
     */
    public int incrementUserFavorite(Long userId) {
        String key = "user:favorite:" + userId;
        return increment(key);
    }
    
    /**
     * 用户收藏文章数量减少
     */
    public int decrementUserFavorite(Long userId) {
        String key = "user:favorite:" + userId;
        return decrement(key);
    }
    
    /**
     * 获取用户收藏文章数量
     */
    public int getUserFavoriteCount(Long userId) {
        String key = "user:favorite:" + userId;
        return getCount(key);
    }
    
    /**
     * 设置用户收藏文章数量
     */
    public void setUserFavoriteCount(Long userId, int count) {
        String key = "user:favorite:" + userId;
        setCount(key, count);
    }
    
    /**
     * 检查用户收藏文章数量是否存在
     */
    public boolean existsUserFavoriteCount(Long userId) {
        String key = "user:favorite:" + userId;
        return exists(key);
    }
    
    /**
     * 删除用户收藏文章数量计数器
     */
    public void deleteUserFavoriteCount(Long userId) {
        String key = "user:favorite:" + userId;
        deleteCounter(key);
    }
    
    // ==================== 标签文章相关 ====================
    
    /**
     * 标签文章数量增加
     */
    public int incrementTagArticle(Integer tagId) {
        String key = "tag:article:" + tagId;
        return increment(key);
    }
    
    /**
     * 标签文章数量减少
     */
    public int decrementTagArticle(Integer tagId) {
        String key = "tag:article:" + tagId;
        return decrement(key);
    }
    
    /**
     * 获取标签文章数量
     */
    public int getTagArticleCount(Integer tagId) {
        String key = "tag:article:" + tagId;
        return getCount(key);
    }
    
    /**
     * 设置标签文章数量
     */
    public void setTagArticleCount(Integer tagId, int count) {
        String key = "tag:article:" + tagId;
        setCount(key, count);
    }
    
    /**
     * 检查标签文章数量是否存在
     */
    public boolean existsTagArticleCount(Integer tagId) {
        String key = "tag:article:" + tagId;
        return exists(key);
    }
    
    /**
     * 删除标签文章数量计数器
     */
    public void deleteTagArticleCount(Integer tagId) {
        String key = "tag:article:" + tagId;
        deleteCounter(key);
    }
    
    // ==================== 作品集文章相关 ====================
    
    /**
     * 作品集文章数量增加
     */
    public int incrementPortfolioArticle(Integer portfolioId) {
        String key = "portfolio:article:" + portfolioId;
        return increment(key);
    }
    
    /**
     * 作品集文章数量减少
     */
    public int decrementPortfolioArticle(Integer portfolioId) {
        String key = "portfolio:article:" + portfolioId;
        return decrement(key);
    }
    
    /**
     * 获取作品集文章数量
     */
    public int getPortfolioArticleCount(Integer portfolioId) {
        String key = "portfolio:article:" + portfolioId;
        return getCount(key);
    }
    
    /**
     * 设置作品集文章数量
     */
    public void setPortfolioArticleCount(Integer portfolioId, int count) {
        String key = "portfolio:article:" + portfolioId;
        setCount(key, count);
    }
    
    /**
     * 检查作品集文章数量是否存在
     */
    public boolean existsPortfolioArticleCount(Integer portfolioId) {
        String key = "portfolio:article:" + portfolioId;
        return exists(key);
    }
    
    /**
     * 删除作品集文章数量计数器
     */
    public void deletePortfolioArticleCount(Integer portfolioId) {
        String key = "portfolio:article:" + portfolioId;
        deleteCounter(key);
    }
    
    // ==================== 用户作品集相关 ====================
    
    /**
     * 用户作品集数量增加
     */
    public int incrementUserPortfolio(Long userId) {
        String key = "user:portfolio:" + userId;
        return increment(key);
    }
    
    /**
     * 用户作品集数量减少
     */
    public int decrementUserPortfolio(Long userId) {
        String key = "user:portfolio:" + userId;
        return decrement(key);
    }
    
    /**
     * 获取用户作品集数量
     */
    public int getUserPortfolioCount(Long userId) {
        String key = "user:portfolio:" + userId;
        return getCount(key);
    }
    
    /**
     * 设置用户作品集数量
     */
    public void setUserPortfolioCount(Long userId, int count) {
        String key = "user:portfolio:" + userId;
        setCount(key, count);
    }
    
    /**
     * 检查用户作品集数量是否存在
     */
    public boolean existsUserPortfolioCount(Long userId) {
        String key = "user:portfolio:" + userId;
        return exists(key);
    }
    
    /**
     * 删除用户作品集数量计数器
     */
    public void deleteUserPortfolioCount(Long userId) {
        String key = "user:portfolio:" + userId;
        deleteCounter(key);
    }
    
    // ==================== 文章评论相关 ====================
    
    /**
     * 文章评论数量增加
     */
    public int incrementArticleComment(Integer articleId) {
        String key = "article:comment:" + articleId;
        return increment(key);
    }
    
    /**
     * 文章评论数量减少
     */
    public int decrementArticleComment(Integer articleId) {
        String key = "article:comment:" + articleId;
        return decrement(key);
    }
    
    /**
     * 获取文章评论数量
     */
    public int getArticleCommentCount(Integer articleId) {
        String key = "article:comment:" + articleId;
        return getCount(key);
    }
    
    /**
     * 设置文章评论数量
     */
    public void setArticleCommentCount(Integer articleId, int count) {
        String key = "article:comment:" + articleId;
        setCount(key, count);
    }
    
    /**
     * 检查文章评论数量是否存在
     */
    public boolean existsArticleCommentCount(Integer articleId) {
        String key = "article:comment:" + articleId;
        return exists(key);
    }
    
    /**
     * 删除文章评论数量计数器
     */
    public void deleteArticleCommentCount(Integer articleId) {
        String key = "article:comment:" + articleId;
        deleteCounter(key);
    }
    
    // ==================== 用户评论相关 ====================
    
    /**
     * 用户评论数量增加
     */
    public int incrementUserComment(Long userId) {
        String key = "user:comment:" + userId;
        return increment(key);
    }
    
    /**
     * 用户评论数量减少
     */
    public int decrementUserComment(Long userId) {
        String key = "user:comment:" + userId;
        return decrement(key);
    }
    
    /**
     * 获取用户评论数量
     */
    public int getUserCommentCount(Long userId) {
        String key = "user:comment:" + userId;
        return getCount(key);
    }
    
    /**
     * 设置用户评论数量
     */
    public void setUserCommentCount(Long userId, int count) {
        String key = "user:comment:" + userId;
        setCount(key, count);
    }
    
    /**
     * 检查用户评论数量是否存在
     */
    public boolean existsUserCommentCount(Long userId) {
        String key = "user:comment:" + userId;
        return exists(key);
    }
    
    /**
     * 删除用户评论数量计数器
     */
    public void deleteUserCommentCount(Long userId) {
        String key = "user:comment:" + userId;
        deleteCounter(key);
    }
    
    // ==================== 用户文章相关 ====================
    
    /**
     * 用户文章数量增加
     */
    public int incrementUserArticle(Long userId) {
        String key = "user:article:" + userId;
        return increment(key);
    }
    
    /**
     * 用户文章数量减少
     */
    public int decrementUserArticle(Long userId) {
        String key = "user:article:" + userId;
        return decrement(key);
    }
    
    /**
     * 获取用户文章数量
     */
    public int getUserArticleCount(Long userId) {
        String key = "user:article:" + userId;
        return getCount(key);
    }
    
    /**
     * 设置用户文章数量
     */
    public void setUserArticleCount(Long userId, int count) {
        String key = "user:article:" + userId;
        setCount(key, count);
    }
    
    /**
     * 检查用户文章数量是否存在
     */
    public boolean existsUserArticleCount(Long userId) {
        String key = "user:article:" + userId;
        return exists(key);
    }
    
    /**
     * 删除用户文章数量计数器
     */
    public void deleteUserArticleCount(Long userId) {
        String key = "user:article:" + userId;
        deleteCounter(key);
    }
    
    // ==================== 管理操作方法 ====================
    
    /**
     * 获取所有计数器键
     */
    public Set<String> getAllCounterKeys() {
        Set<String> keys = redisTemplate.keys(COUNTER_PREFIX + "*");
        return keys != null ? keys : java.util.Collections.emptySet();
    }
    
    /**
     * 获取计数器总数
     */
    public int getTotalCounters() {
        Set<String> keys = redisTemplate.keys(COUNTER_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }
    
    /**
     * 检查计数器是否存在（不包含前缀）
     */
    public boolean hasCounter(String key) {
        return exists(key);
    }
    
    /**
     * 获取计数器的过期时间（秒）
     */
    public long getExpire(String key) {
        String redisKey = COUNTER_PREFIX + key;
        Long expire = redisTemplate.getExpire(redisKey);
        return expire != null ? expire : -1;
    }
    
    /**
     * 设置计数器过期时间
     */
    public boolean setExpire(String key, long timeout) {
        String redisKey = COUNTER_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(timeout)));
    }
}
