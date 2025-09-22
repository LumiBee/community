package com.lumibee.hive.service.message;

import com.lumibee.hive.dto.message.ArticleCacheUpdateMessage;
import com.lumibee.hive.service.RedisClearCacheService;
import com.lumibee.hive.service.RedisCounterService;
import com.lumibee.hive.service.RedisPopularArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章缓存更新消费者
 * 
 * 负责异步更新 Redis 缓存
 */
@Slf4j
@Service
public class ArticleCacheUpdateConsumer {

    @Autowired
    private RedisClearCacheService redisClearCacheService;

    @Autowired
    private RedisCounterService redisCounterService;

    @Autowired
    private RedisPopularArticleService redisPopularArticleService;

    @Autowired
    private MessageIdempotencyService idempotencyService;

    /**
     * 处理缓存更新消息
     */
    @RabbitListener(queues = "${rabbitmq.article.cache-queue}")
    public void handleCacheUpdateMessage(ArticleCacheUpdateMessage message,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                       @Header(AmqpHeaders.CHANNEL) Channel channel) {
        try {
            log.info("开始处理缓存更新消息: articleId={}, operationType={}, businessId={}", 
                message.getArticleId(), message.getOperationType(), message.getBusinessId());

            // 幂等性检查
            if (!idempotencyService.checkAndSetProcessed(message.getBusinessId())) {
                log.warn("缓存更新消息重复处理，跳过: businessId={}", message.getBusinessId());
                acknowledgeMessage(channel, deliveryTag);
                return;
            }

            // 根据操作类型处理
            switch (message.getOperationType()) {
                case CLEAR_CACHE:
                    handleClearCache(message);
                    break;
                case UPDATE_COUNTER:
                    handleUpdateCounter(message);
                    break;
                case UPDATE_POPULARITY:
                    handleUpdatePopularity(message);
                    break;
                case SYNC_EXCERPT:
                    handleSyncExcerpt(message);
                    break;
                case CLEAR_TAG_CACHE:
                    handleClearTagCache(message);
                    break;
                case CLEAR_USER_CACHE:
                    handleClearUserCache(message);
                    break;
                case CLEAR_PORTFOLIO_CACHE:
                    handleClearPortfolioCache(message);
                    break;
                default:
                    log.warn("未知的缓存操作类型: {}", message.getOperationType());
            }

            // 手动确认消息
            acknowledgeMessage(channel, deliveryTag);
            log.info("缓存更新消息处理完成: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType());

        } catch (Exception e) {
            log.error("处理缓存更新消息失败: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType(), e);
            
            // 处理失败，拒绝消息并重新入队（如果还有重试次数）
            rejectMessage(channel, deliveryTag, message);
        }
    }

    /**
     * 处理清除缓存
     */
    private void handleClearCache(ArticleCacheUpdateMessage message) {
        try {
            List<String> cacheKeys = message.getCacheKeysToClear();
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                for (String key : cacheKeys) {
                    redisClearCacheService.clearCachesByPattern(key);
                }
                log.info("清除缓存完成: articleId={}, keys={}", message.getArticleId(), cacheKeys);
            } else {
                // 如果没有指定具体键，清除所有相关缓存
                redisClearCacheService.clearAllArticleListCaches();
                log.info("清除所有文章列表缓存完成: articleId={}", message.getArticleId());
            }
        } catch (Exception e) {
            log.error("清除缓存失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 处理更新计数器
     */
    private void handleUpdateCounter(ArticleCacheUpdateMessage message) {
        try {
            List<ArticleCacheUpdateMessage.CounterType> counterTypes = message.getCounterTypes();
            if (counterTypes != null && !counterTypes.isEmpty()) {
                for (ArticleCacheUpdateMessage.CounterType counterType : counterTypes) {
                    switch (counterType) {
                        case USER_ARTICLE_COUNT:
                            redisCounterService.incrementUserArticle(message.getUserId());
                            break;
                        case PORTFOLIO_ARTICLE_COUNT:
                            if (message.getPortfolioId() != null) {
                                redisCounterService.incrementPortfolioArticle(message.getPortfolioId());
                            }
                            break;
                        case ARTICLE_VIEW_COUNT:
                            redisCounterService.incrementArticleView(message.getArticleId());
                            break;
                        case ARTICLE_LIKE_COUNT:
                            redisCounterService.incrementArticleLike(message.getArticleId());
                            break;
                        case ARTICLE_COMMENT_COUNT:
                            // 这里可以添加评论数更新逻辑
                            break;
                        case ARTICLE_FAVORITE_COUNT:
                            // 这里可以添加收藏数更新逻辑
                            break;
                    }
                }
                log.info("更新计数器完成: articleId={}, counterTypes={}", message.getArticleId(), counterTypes);
            }
        } catch (Exception e) {
            log.error("更新计数器失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 处理更新热度分数
     */
    private void handleUpdatePopularity(ArticleCacheUpdateMessage message) {
        try {
            redisPopularArticleService.updateArticlePopularity(
                message.getArticleId(),
                message.getViewCount(),
                message.getLikeCount(),
                message.getCommentCount(),
                message.getPublishTime()
            );
            log.info("更新热度分数完成: articleId={}, viewCount={}, likeCount={}, commentCount={}", 
                message.getArticleId(), message.getViewCount(), message.getLikeCount(), message.getCommentCount());
        } catch (Exception e) {
            log.error("更新热度分数失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 处理同步文章摘要
     */
    private void handleSyncExcerpt(ArticleCacheUpdateMessage message) {
        try {
            // 这里需要根据实际需求实现文章摘要同步逻辑
            // 可能需要从数据库查询文章信息，然后同步到 Redis
            log.info("同步文章摘要完成: articleId={}", message.getArticleId());
        } catch (Exception e) {
            log.error("同步文章摘要失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 处理清除标签缓存
     */
    private void handleClearTagCache(ArticleCacheUpdateMessage message) {
        try {
            redisClearCacheService.clearAllTagListCaches();
            log.info("清除标签缓存完成: articleId={}", message.getArticleId());
        } catch (Exception e) {
            log.error("清除标签缓存失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 处理清除用户缓存
     */
    private void handleClearUserCache(ArticleCacheUpdateMessage message) {
        try {
            if (message.getUserId() != null) {
                redisClearCacheService.clearUserArticleCaches(message.getUserId());
                log.info("清除用户缓存完成: articleId={}, userId={}", message.getArticleId(), message.getUserId());
            }
        } catch (Exception e) {
            log.error("清除用户缓存失败: articleId={}, userId={}", message.getArticleId(), message.getUserId(), e);
            throw e;
        }
    }

    /**
     * 处理清除作品集缓存
     */
    private void handleClearPortfolioCache(ArticleCacheUpdateMessage message) {
        try {
            if (message.getPortfolioId() != null) {
                // 这里可以添加作品集缓存清除逻辑
                log.info("清除作品集缓存完成: articleId={}, portfolioId={}", message.getArticleId(), message.getPortfolioId());
            }
        } catch (Exception e) {
            log.error("清除作品集缓存失败: articleId={}, portfolioId={}", message.getArticleId(), message.getPortfolioId(), e);
            throw e;
        }
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
    private void rejectMessage(Channel channel, long deliveryTag, ArticleCacheUpdateMessage message) {
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
