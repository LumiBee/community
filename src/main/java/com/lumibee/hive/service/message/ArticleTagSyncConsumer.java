package com.lumibee.hive.service.message;

import com.lumibee.hive.dto.message.ArticleTagSyncMessage;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.model.Tag;
import com.lumibee.hive.service.RedisClearCacheService;
import com.lumibee.hive.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 文章标签同步消费者
 * 
 * 负责异步处理文章标签相关操作
 */
@Slf4j
@Service
public class ArticleTagSyncConsumer {

    @Autowired
    private TagService tagService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedisClearCacheService redisClearCacheService;

    @Autowired
    private MessageIdempotencyService idempotencyService;

    /**
     * 处理标签同步消息
     */
    @RabbitListener(queues = "${rabbitmq.article.tag-queue}")
    public void handleTagSyncMessage(ArticleTagSyncMessage message,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                   @Header(AmqpHeaders.CHANNEL) Channel channel) {
        try {
            log.info("开始处理标签同步消息: articleId={}, operationType={}, businessId={}", 
                message.getArticleId(), message.getOperationType(), message.getBusinessId());

            // 幂等性检查
            if (!idempotencyService.checkAndSetProcessed(message.getBusinessId())) {
                log.warn("标签同步消息重复处理，跳过: businessId={}", message.getBusinessId());
                acknowledgeMessage(channel, deliveryTag);
                return;
            }

            // 根据操作类型处理
            switch (message.getOperationType()) {
                case CREATE_TAGS:
                    handleCreateTags(message);
                    break;
                case UPDATE_TAG_RELATIONS:
                    handleUpdateTagRelations(message);
                    break;
                case DELETE_TAG_RELATIONS:
                    handleDeleteTagRelations(message);
                    break;
                case UPDATE_TAG_COUNT:
                    handleUpdateTagCount(message);
                    break;
                case CLEAR_TAG_CACHE:
                    handleClearTagCache(message);
                    break;
                default:
                    log.warn("未知的标签操作类型: {}", message.getOperationType());
            }

            // 手动确认消息
            acknowledgeMessage(channel, deliveryTag);
            log.info("标签同步消息处理完成: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType());

        } catch (Exception e) {
            log.error("处理标签同步消息失败: articleId={}, operationType={}", 
                message.getArticleId(), message.getOperationType(), e);
            
            // 处理失败，拒绝消息并重新入队（如果还有重试次数）
            rejectMessage(channel, deliveryTag, message);
        }
    }

    /**
     * 处理创建标签
     */
    private void handleCreateTags(ArticleTagSyncMessage message) {
        try {
            List<String> tagNames = message.getTagNames();
            if (tagNames != null && !tagNames.isEmpty()) {
                Set<Tag> tags = tagService.selectOrCreateTags(tagNames);
                log.info("创建标签完成: articleId={}, tagNames={}, createdTags={}", 
                    message.getArticleId(), tagNames, tags.size());
            }
        } catch (Exception e) {
            log.error("创建标签失败: articleId={}, tagNames={}", message.getArticleId(), message.getTagNames(), e);
            throw e;
        }
    }

    /**
     * 处理更新标签关系
     */
    private void handleUpdateTagRelations(ArticleTagSyncMessage message) {
        try {
            Integer articleId = message.getArticleId();
            List<String> tagNames = message.getTagNames();

            if (tagNames != null && !tagNames.isEmpty()) {
                // 先删除现有标签关系
                articleMapper.deleteArticleTagByArticleId(articleId);
                
                // 创建或获取标签
                Set<Tag> tags = tagService.selectOrCreateTags(tagNames);
                
                // 建立新的标签关系
                for (Tag tag : tags) {
                    if (tag != null) {
                        tagService.insertTagArticleRelation(articleId, tag.getTagId());
                    }
                }
                
                log.info("更新标签关系完成: articleId={}, tagNames={}, tagCount={}", 
                    articleId, tagNames, tags.size());
            }
        } catch (Exception e) {
            log.error("更新标签关系失败: articleId={}, tagNames={}", message.getArticleId(), message.getTagNames(), e);
            throw e;
        }
    }

    /**
     * 处理删除标签关系
     */
    private void handleDeleteTagRelations(ArticleTagSyncMessage message) {
        try {
            Integer articleId = message.getArticleId();
            articleMapper.deleteArticleTagByArticleId(articleId);
            log.info("删除标签关系完成: articleId={}", articleId);
        } catch (Exception e) {
            log.error("删除标签关系失败: articleId={}", message.getArticleId(), e);
            throw e;
        }
    }

    /**
     * 处理更新标签计数
     */
    private void handleUpdateTagCount(ArticleTagSyncMessage message) {
        try {
            List<Integer> tagIds = message.getTagIds();
            if (tagIds != null && !tagIds.isEmpty()) {
                for (Integer tagId : tagIds) {
                    tagService.incrementArticleCount(tagId);
                }
                log.info("更新标签计数完成: articleId={}, tagIds={}", message.getArticleId(), tagIds);
            }
        } catch (Exception e) {
            log.error("更新标签计数失败: articleId={}, tagIds={}", message.getArticleId(), message.getTagIds(), e);
            throw e;
        }
    }

    /**
     * 处理清除标签缓存
     */
    private void handleClearTagCache(ArticleTagSyncMessage message) {
        try {
            List<String> cacheKeys = message.getTagCacheKeys();
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                for (String key : cacheKeys) {
                    redisClearCacheService.clearCachesByPattern(key);
                }
                log.info("清除指定标签缓存完成: articleId={}, cacheKeys={}", message.getArticleId(), cacheKeys);
            } else {
                // 如果没有指定具体键，清除所有标签缓存
                redisClearCacheService.clearAllTagListCaches();
                log.info("清除所有标签缓存完成: articleId={}", message.getArticleId());
            }
        } catch (Exception e) {
            log.error("清除标签缓存失败: articleId={}", message.getArticleId(), e);
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
    private void rejectMessage(Channel channel, long deliveryTag, ArticleTagSyncMessage message) {
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
