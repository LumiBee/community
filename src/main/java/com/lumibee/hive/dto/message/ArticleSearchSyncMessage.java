package com.lumibee.hive.dto.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章搜索同步消息
 * 
 * 用于异步同步文章数据到 Elasticsearch
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleSearchSyncMessage extends BaseMessage {

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章摘要
     */
    private String excerpt;

    /**
     * 文章slug
     */
    private String slug;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 文章修改时间
     */
    private String gmtModified;

    /**
     * 背景图片URL
     */
    private String backgroundUrl;

    /**
     * 操作类型：CREATE, UPDATE, DELETE
     */
    private String operationType;

    public ArticleSearchSyncMessage() {
        super();
        this.setMessageType("ARTICLE_SEARCH_SYNC");
        this.setSource("ARTICLE_SERVICE");
    }

    public ArticleSearchSyncMessage(Integer articleId, String businessId, String operationType) {
        super(businessId, "ARTICLE_SEARCH_SYNC", "ARTICLE_SERVICE");
        this.articleId = articleId;
        this.operationType = operationType;
    }

    /**
     * 创建索引消息
     */
    public static ArticleSearchSyncMessage createIndexMessage(Integer articleId, String businessId) {
        return new ArticleSearchSyncMessage(articleId, businessId, "CREATE");
    }

    /**
     * 更新索引消息
     */
    public static ArticleSearchSyncMessage updateIndexMessage(Integer articleId, String businessId) {
        return new ArticleSearchSyncMessage(articleId, businessId, "UPDATE");
    }

    /**
     * 删除索引消息
     */
    public static ArticleSearchSyncMessage deleteIndexMessage(Integer articleId, String businessId) {
        return new ArticleSearchSyncMessage(articleId, businessId, "DELETE");
    }
}
