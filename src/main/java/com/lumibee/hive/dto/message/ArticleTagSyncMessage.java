package com.lumibee.hive.dto.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文章标签同步消息
 * 
 * 用于异步处理文章标签相关操作
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleTagSyncMessage extends BaseMessage {

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 标签操作类型
     */
    private TagOperationType operationType;

    /**
     * 标签名称列表
     */
    private List<String> tagNames;

    /**
     * 标签ID列表
     */
    private List<Integer> tagIds;

    /**
     * 需要清除的标签缓存键
     */
    private List<String> tagCacheKeys;

    /**
     * 标签操作类型枚举
     */
    public enum TagOperationType {
        /**
         * 创建标签
         */
        CREATE_TAGS,
        /**
         * 更新标签关系
         */
        UPDATE_TAG_RELATIONS,
        /**
         * 删除标签关系
         */
        DELETE_TAG_RELATIONS,
        /**
         * 更新标签计数
         */
        UPDATE_TAG_COUNT,
        /**
         * 清除标签缓存
         */
        CLEAR_TAG_CACHE
    }

    public ArticleTagSyncMessage() {
        super();
        this.setMessageType("ARTICLE_TAG_SYNC");
        this.setSource("ARTICLE_SERVICE");
    }

    public ArticleTagSyncMessage(Integer articleId, String businessId, TagOperationType operationType) {
        super(businessId, "ARTICLE_TAG_SYNC", "ARTICLE_SERVICE");
        this.articleId = articleId;
        this.operationType = operationType;
    }

    /**
     * 创建标签处理消息
     */
    public static ArticleTagSyncMessage createTagProcessMessage(Integer articleId, String businessId, List<String> tagNames) {
        ArticleTagSyncMessage message = new ArticleTagSyncMessage(articleId, businessId, TagOperationType.CREATE_TAGS);
        message.setTagNames(tagNames);
        return message;
    }

    /**
     * 创建标签关系更新消息
     */
    public static ArticleTagSyncMessage createTagRelationMessage(Integer articleId, String businessId, 
                                                               List<String> tagNames, List<Integer> tagIds) {
        ArticleTagSyncMessage message = new ArticleTagSyncMessage(articleId, businessId, TagOperationType.UPDATE_TAG_RELATIONS);
        message.setTagNames(tagNames);
        message.setTagIds(tagIds);
        return message;
    }

    /**
     * 创建标签计数更新消息
     */
    public static ArticleTagSyncMessage createTagCountMessage(Integer articleId, String businessId, List<Integer> tagIds) {
        ArticleTagSyncMessage message = new ArticleTagSyncMessage(articleId, businessId, TagOperationType.UPDATE_TAG_COUNT);
        message.setTagIds(tagIds);
        return message;
    }

    /**
     * 创建清除标签缓存消息
     */
    public static ArticleTagSyncMessage createClearTagCacheMessage(Integer articleId, String businessId, List<String> cacheKeys) {
        ArticleTagSyncMessage message = new ArticleTagSyncMessage(articleId, businessId, TagOperationType.CLEAR_TAG_CACHE);
        message.setTagCacheKeys(cacheKeys);
        return message;
    }
}
