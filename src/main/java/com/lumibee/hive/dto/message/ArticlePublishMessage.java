package com.lumibee.hive.dto.message;

import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章发布消息
 * 
 * 用于异步处理文章发布后的各种操作
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticlePublishMessage extends BaseMessage {

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 是否为新文章
     */
    private Boolean isNewArticle;

    /**
     * 作品集ID
     */
    private String portfolioName;

    /**
     * 标签名称列表
     */
    private java.util.List<String> tagsName;

    /**
     * 文章发布时间
     */
    private java.time.LocalDateTime publishTime;

    public ArticlePublishMessage() {
        super();
        this.setMessageType("ARTICLE_PUBLISH");
        this.setSource("ARTICLE_SERVICE");
    }

    public ArticlePublishMessage(Integer articleId, Long userId, String businessId) {
        super(businessId, "ARTICLE_PUBLISH", "ARTICLE_SERVICE");
        this.articleId = articleId;
        this.userId = userId;
    }

    /**
     * 从 ArticlePublishRequestDTO 创建消息
     */
    public static ArticlePublishMessage fromRequest(ArticlePublishRequestDTO request, Integer articleId, Long userId, String businessId) {
        ArticlePublishMessage message = new ArticlePublishMessage(articleId, userId, businessId);
        message.setTitle(request.getTitle());
        message.setContent(request.getContent());
        message.setExcerpt(request.getExcerpt());
        message.setPortfolioName(request.getPortfolioName());
        message.setTagsName(request.getTagsName());
        message.setPublishTime(java.time.LocalDateTime.now());
        return message;
    }
}
