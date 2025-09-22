package com.lumibee.hive.dto.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文章缓存更新消息
 * 
 * 用于异步更新 Redis 缓存
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleCacheUpdateMessage extends BaseMessage {

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 作品集ID
     */
    private Integer portfolioId;

    /**
     * 缓存操作类型
     */
    private CacheOperationType operationType;

    /**
     * 需要清除的缓存键列表
     */
    private List<String> cacheKeysToClear;

    /**
     * 需要更新的计数器类型
     */
    private List<CounterType> counterTypes;

    /**
     * 文章热度分数
     */
    private Double popularityScore;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 文章发布时间
     */
    private java.time.LocalDateTime publishTime;

    /**
     * 缓存操作类型枚举
     */
    public enum CacheOperationType {
        /**
         * 清除缓存
         */
        CLEAR_CACHE,
        /**
         * 更新计数器
         */
        UPDATE_COUNTER,
        /**
         * 更新热度分数
         */
        UPDATE_POPULARITY,
        /**
         * 同步文章摘要
         */
        SYNC_EXCERPT,
        /**
         * 清除标签缓存
         */
        CLEAR_TAG_CACHE,
        /**
         * 清除用户缓存
         */
        CLEAR_USER_CACHE,
        /**
         * 清除作品集缓存
         */
        CLEAR_PORTFOLIO_CACHE
    }

    /**
     * 计数器类型枚举
     */
    public enum CounterType {
        /**
         * 用户文章计数
         */
        USER_ARTICLE_COUNT,
        /**
         * 作品集文章计数
         */
        PORTFOLIO_ARTICLE_COUNT,
        /**
         * 文章浏览量
         */
        ARTICLE_VIEW_COUNT,
        /**
         * 文章点赞数
         */
        ARTICLE_LIKE_COUNT,
        /**
         * 文章评论数
         */
        ARTICLE_COMMENT_COUNT,
        /**
         * 文章收藏数
         */
        ARTICLE_FAVORITE_COUNT
    }

    public ArticleCacheUpdateMessage() {
        super();
        this.setMessageType("ARTICLE_CACHE_UPDATE");
        this.setSource("ARTICLE_SERVICE");
    }

    public ArticleCacheUpdateMessage(Integer articleId, String businessId, CacheOperationType operationType) {
        super(businessId, "ARTICLE_CACHE_UPDATE", "ARTICLE_SERVICE");
        this.articleId = articleId;
        this.operationType = operationType;
    }

    /**
     * 创建清除缓存消息
     */
    public static ArticleCacheUpdateMessage createClearCacheMessage(Integer articleId, String businessId, List<String> cacheKeys) {
        ArticleCacheUpdateMessage message = new ArticleCacheUpdateMessage(articleId, businessId, CacheOperationType.CLEAR_CACHE);
        message.setCacheKeysToClear(cacheKeys);
        return message;
    }

    /**
     * 创建更新计数器消息
     */
    public static ArticleCacheUpdateMessage createUpdateCounterMessage(Integer articleId, String businessId, List<CounterType> counterTypes) {
        ArticleCacheUpdateMessage message = new ArticleCacheUpdateMessage(articleId, businessId, CacheOperationType.UPDATE_COUNTER);
        message.setCounterTypes(counterTypes);
        return message;
    }

    /**
     * 创建更新热度分数消息
     */
    public static ArticleCacheUpdateMessage createUpdatePopularityMessage(Integer articleId, String businessId, 
                                                                         Integer viewCount, Integer likeCount, Integer commentCount,
                                                                         java.time.LocalDateTime publishTime) {
        ArticleCacheUpdateMessage message = new ArticleCacheUpdateMessage(articleId, businessId, CacheOperationType.UPDATE_POPULARITY);
        message.setViewCount(viewCount);
        message.setLikeCount(likeCount);
        message.setCommentCount(commentCount);
        message.setPublishTime(publishTime);
        return message;
    }
}
