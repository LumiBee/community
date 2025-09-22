package com.lumibee.hive.service.message;

import com.lumibee.hive.config.RabbitMQProperties;
import com.lumibee.hive.dto.message.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import io.micrometer.core.instrument.Timer;

/**
 * 消息生产者服务
 * 
 * 负责发送各种业务消息到 RabbitMQ
 */
@Slf4j
@Service
public class MessageProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQProperties rabbitMQProperties;

    @Autowired
    private RabbitMQMetricsCollector metricsCollector;

    /**
     * 发送文章发布消息
     */
    public void sendArticlePublishMessage(ArticlePublishMessage message) {
        Timer.Sample sample = metricsCollector.startMessageSendTimer();
        try {
            String businessId = message.getBusinessId() != null ? message.getBusinessId() : generateBusinessId(message.getArticleId(), "PUBLISH");
            message.setBusinessId(businessId);
            
            rabbitTemplate.convertAndSend(
                (String) rabbitMQProperties.getArticle().getExchange(),
                (String) rabbitMQProperties.getArticle().getRoutingKey(),
                (Object) message
            );
            
            metricsCollector.recordMessageSent("ARTICLE_PUBLISH", true);
            log.info("文章发布消息发送成功: articleId={}, businessId={}", message.getArticleId(), businessId);
        } catch (Exception e) {
            metricsCollector.recordMessageSent("ARTICLE_PUBLISH", false);
            log.error("文章发布消息发送失败: articleId={}", message.getArticleId(), e);
            throw new RuntimeException("文章发布消息发送失败", e);
        } finally {
            metricsCollector.stopMessageSendTimer(sample, "ARTICLE_PUBLISH");
        }
    }

    /**
     * 发送搜索同步消息
     */
    public void sendSearchSyncMessage(ArticleSearchSyncMessage message) {
        Timer.Sample sample = metricsCollector.startMessageSendTimer();
        try {
            String businessId = message.getBusinessId() != null ? message.getBusinessId() : generateBusinessId(message.getArticleId(), "SEARCH_SYNC");
            message.setBusinessId(businessId);
            
            rabbitTemplate.convertAndSend(
                (String) rabbitMQProperties.getArticle().getExchange(),
                (String) rabbitMQProperties.getArticle().getSearchRoutingKey(),
                (Object) message
            );
            
            metricsCollector.recordMessageSent("SEARCH_SYNC", true);
            log.info("搜索同步消息发送成功: articleId={}, operationType={}, businessId={}", 
                message.getArticleId(), message.getOperationType(), businessId);
        } catch (Exception e) {
            metricsCollector.recordMessageSent("SEARCH_SYNC", false);
            log.error("搜索同步消息发送失败: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType(), e);
            throw new RuntimeException("搜索同步消息发送失败", e);
        } finally {
            metricsCollector.stopMessageSendTimer(sample, "SEARCH_SYNC");
        }
    }

    /**
     * 发送缓存更新消息
     */
    public void sendCacheUpdateMessage(ArticleCacheUpdateMessage message) {
        Timer.Sample sample = metricsCollector.startMessageSendTimer();
        try {
            String businessId = message.getBusinessId() != null ? message.getBusinessId() : generateBusinessId(message.getArticleId(), "CACHE_UPDATE");
            message.setBusinessId(businessId);
            
            rabbitTemplate.convertAndSend(
                (String) rabbitMQProperties.getArticle().getExchange(),
                (String) rabbitMQProperties.getArticle().getCacheRoutingKey(),
                (Object) message
            );
            
            metricsCollector.recordMessageSent("CACHE_UPDATE", true);
            log.info("缓存更新消息发送成功: articleId={}, operationType={}, businessId={}", 
                message.getArticleId(), message.getOperationType(), businessId);
        } catch (Exception e) {
            metricsCollector.recordMessageSent("CACHE_UPDATE", false);
            log.error("缓存更新消息发送失败: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType(), e);
            throw new RuntimeException("缓存更新消息发送失败", e);
        } finally {
            metricsCollector.stopMessageSendTimer(sample, "CACHE_UPDATE");
        }
    }

    /**
     * 发送标签同步消息
     */
    public void sendTagSyncMessage(ArticleTagSyncMessage message) {
        Timer.Sample sample = metricsCollector.startMessageSendTimer();
        try {
            String businessId = message.getBusinessId() != null ? message.getBusinessId() : generateBusinessId(message.getArticleId(), "TAG_SYNC");
            message.setBusinessId(businessId);
            
            rabbitTemplate.convertAndSend(
                (String) rabbitMQProperties.getArticle().getExchange(),
                (String) rabbitMQProperties.getArticle().getTagRoutingKey(),
                (Object) message
            );
            
            metricsCollector.recordMessageSent("TAG_SYNC", true);
            log.info("标签同步消息发送成功: articleId={}, operationType={}, businessId={}", 
                message.getArticleId(), message.getOperationType(), businessId);
        } catch (Exception e) {
            metricsCollector.recordMessageSent("TAG_SYNC", false);
            log.error("标签同步消息发送失败: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType(), e);
            throw new RuntimeException("标签同步消息发送失败", e);
        } finally {
            metricsCollector.stopMessageSendTimer(sample, "TAG_SYNC");
        }
    }

    /**
     * 批量发送消息
     */
    public void sendBatchMessages(List<BaseMessage> messages) {
        for (BaseMessage message : messages) {
            try {
                if (message instanceof ArticlePublishMessage) {
                    sendArticlePublishMessage((ArticlePublishMessage) message);
                } else if (message instanceof ArticleSearchSyncMessage) {
                    sendSearchSyncMessage((ArticleSearchSyncMessage) message);
                } else if (message instanceof ArticleCacheUpdateMessage) {
                    sendCacheUpdateMessage((ArticleCacheUpdateMessage) message);
                } else if (message instanceof ArticleTagSyncMessage) {
                    sendTagSyncMessage((ArticleTagSyncMessage) message);
                } else {
                    log.warn("未知的消息类型: {}", message.getClass().getSimpleName());
                }
            } catch (Exception e) {
                log.error("批量发送消息失败: messageType={}, messageId={}", 
                    message.getMessageType(), message.getMessageId(), e);
                // 继续处理其他消息，不中断整个批量操作
            }
        }
    }

    /**
     * 发送重试消息
     */
    public void sendRetryMessage(BaseMessage message) {
        try {
            message.incrementRetryCount();
            
            if (!message.canRetry()) {
                log.error("消息重试次数已达上限，发送到死信队列: messageId={}, retryCount={}", 
                    message.getMessageId(), message.getRetryCount());
                sendToDeadLetterQueue(message);
                return;
            }
            
            metricsCollector.recordMessageRetried(message.getMessageType());
            
            // 根据消息类型发送到对应队列
            if (message instanceof ArticlePublishMessage) {
                sendArticlePublishMessage((ArticlePublishMessage) message);
            } else if (message instanceof ArticleSearchSyncMessage) {
                sendSearchSyncMessage((ArticleSearchSyncMessage) message);
            } else if (message instanceof ArticleCacheUpdateMessage) {
                sendCacheUpdateMessage((ArticleCacheUpdateMessage) message);
            } else if (message instanceof ArticleTagSyncMessage) {
                sendTagSyncMessage((ArticleTagSyncMessage) message);
            }
            
            log.info("重试消息发送成功: messageId={}, retryCount={}", 
                message.getMessageId(), message.getRetryCount());
        } catch (Exception e) {
            log.error("重试消息发送失败: messageId={}, retryCount={}", 
                message.getMessageId(), message.getRetryCount(), e);
        }
    }

    /**
     * 发送消息到死信队列
     */
    public void sendToDeadLetterQueue(BaseMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                (String) rabbitMQProperties.getDeadLetter().getExchange(),
                (String) rabbitMQProperties.getDeadLetter().getRoutingKey(),
                (Object) message
            );
            
            metricsCollector.recordMessageDeadLettered(message.getMessageType());
            log.warn("消息发送到死信队列: messageId={}, messageType={}, retryCount={}", 
                message.getMessageId(), message.getMessageType(), message.getRetryCount());
        } catch (Exception e) {
            log.error("发送消息到死信队列失败: messageId={}", message.getMessageId(), e);
        }
    }

    /**
     * 生成业务唯一ID
     */
    private String generateBusinessId(Integer articleId, String operation) {
        return String.format("article_%d_%s_%s", articleId, operation, UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * 检查消息队列连接状态
     */
    public boolean isConnectionHealthy() {
        try {
            // 发送一个测试消息来检查连接
            rabbitTemplate.convertAndSend(
                (String) rabbitMQProperties.getArticle().getExchange(),
                (String) rabbitMQProperties.getArticle().getRoutingKey(),
                (Object) "health_check"
            );
            return true;
        } catch (Exception e) {
            log.error("RabbitMQ 连接检查失败", e);
            return false;
        }
    }
}
