package com.lumibee.hive.service.message;

import com.lumibee.hive.dto.message.ArticleSearchSyncMessage;
import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.service.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Timer;

/**
 * 文章搜索同步消费者
 * 
 * 负责异步同步文章数据到 Elasticsearch
 */
@Slf4j
@Service
public class ArticleSearchSyncConsumer {

    @Autowired
    private ArticleRepository articleRepository;


    @Autowired
    private MessageIdempotencyService idempotencyService;

    @Autowired
    private RabbitMQMetricsCollector metricsCollector;

    /**
     * 处理文章搜索同步消息
     */
    @RabbitListener(queues = "${rabbitmq.article.search-queue}")
    public void handleSearchSyncMessage(ArticleSearchSyncMessage message,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                      @Header(AmqpHeaders.CHANNEL) Channel channel) {
        Timer.Sample sample = metricsCollector.startMessageProcessingTimer();
        try {
            log.info("开始处理搜索同步消息: articleId={}, operationType={}, businessId={}", 
                message.getArticleId(), message.getOperationType(), message.getBusinessId());

            // 幂等性检查
            if (!idempotencyService.checkAndSetProcessed(message.getBusinessId())) {
                metricsCollector.recordIdempotencyCheck(true);
                log.warn("搜索同步消息重复处理，跳过: businessId={}", message.getBusinessId());
                acknowledgeMessage(channel, deliveryTag);
                return;
            }
            metricsCollector.recordIdempotencyCheck(false);

            // 根据操作类型处理
            switch (message.getOperationType()) {
                case "CREATE":
                    handleCreateIndex(message);
                    break;
                case "UPDATE":
                    handleUpdateIndex(message);
                    break;
                case "DELETE":
                    handleDeleteIndex(message);
                    break;
                default:
                    log.warn("未知的操作类型: {}", message.getOperationType());
            }

            // 手动确认消息
            acknowledgeMessage(channel, deliveryTag);
            metricsCollector.recordMessageConsumed("SEARCH_SYNC", true);
            log.info("搜索同步消息处理完成: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType());

        } catch (Exception e) {
            metricsCollector.recordMessageConsumed("SEARCH_SYNC", false);
            log.error("处理搜索同步消息失败: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType(), e);
            
            // 处理失败，拒绝消息并重新入队（如果还有重试次数）
            rejectMessage(channel, deliveryTag, message);
        } finally {
            metricsCollector.stopMessageProcessingTimer(sample, "SEARCH_SYNC");
        }
    }

    /**
     * 处理创建索引
     */
    private void handleCreateIndex(ArticleSearchSyncMessage message) {
        try {
            ArticleDocument document = buildArticleDocument(message);
            articleRepository.save(document);
            log.info("文章索引创建成功: articleId={}", message.getArticleId());
        } catch (Exception e) {
            log.error("创建文章索引失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 处理更新索引
     */
    private void handleUpdateIndex(ArticleSearchSyncMessage message) {
        try {
            ArticleDocument document = buildArticleDocument(message);
            articleRepository.save(document);
            log.info("文章索引更新成功: articleId={}", message.getArticleId());
        } catch (Exception e) {
            log.error("更新文章索引失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 处理删除索引
     */
    private void handleDeleteIndex(ArticleSearchSyncMessage message) {
        try {
            articleRepository.deleteById(message.getArticleId());
            log.info("文章索引删除成功: articleId={}", message.getArticleId());
        } catch (Exception e) {
            log.error("删除文章索引失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 构建文章文档
     */
    private ArticleDocument buildArticleDocument(ArticleSearchSyncMessage message) {
        ArticleDocument document = new ArticleDocument();
        document.setId(message.getArticleId());
        document.setTitle(message.getTitle());
        document.setContent(message.getContent());
        document.setExcerpt(message.getExcerpt());
        document.setUserName(message.getUserName());
        document.setSlug(message.getSlug());
        document.setLikes(message.getLikes());
        document.setViewCount(message.getViewCount());
        document.setAvatarUrl(message.getAvatarUrl());
        document.setGmtModified(message.getGmtModified());
        document.setBackgroundUrl(message.getBackgroundUrl());
        document.setUserId(message.getUserId());
        return document;
    }

    /**
     * 确认消息
     */
    private void acknowledgeMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("确认消息失败: deliveryTag={}", deliveryTag, e);
        }
    }

    /**
     * 拒绝消息
     */
    private void rejectMessage(Channel channel, long deliveryTag, ArticleSearchSyncMessage message) {
        try {
            // 如果还有重试次数，重新入队；否则拒绝并发送到死信队列
            boolean requeue = message.canRetry();
            channel.basicNack(deliveryTag, false, requeue);
            
            if (requeue) {
                log.info("消息重新入队: articleId={}, retryCount={}", message.getArticleId(), message.getRetryCount());
            } else {
                log.warn("消息发送到死信队列: articleId={}, retryCount={}", message.getArticleId(), message.getRetryCount());
            }
        } catch (Exception e) {
            log.error("拒绝消息失败: deliveryTag={}", deliveryTag, e);
        }
    }
}
