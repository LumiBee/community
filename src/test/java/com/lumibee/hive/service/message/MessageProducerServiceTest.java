package com.lumibee.hive.service.message;

import com.lumibee.hive.config.RabbitMQProperties;
import com.lumibee.hive.dto.message.ArticleCacheUpdateMessage;
import com.lumibee.hive.dto.message.ArticlePublishMessage;
import com.lumibee.hive.dto.message.ArticleSearchSyncMessage;
import com.lumibee.hive.dto.message.ArticleTagSyncMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 消息生产者服务测试
 */
@ExtendWith(MockitoExtension.class)
class MessageProducerServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitMQProperties rabbitMQProperties;

    @Mock
    private RabbitMQMetricsCollector metricsCollector;

    @InjectMocks
    private MessageProducerService messageProducerService;

    private RabbitMQProperties.ArticleConfig articleConfig;
    private RabbitMQProperties.DeadLetterConfig deadLetterConfig;

    @BeforeEach
    void setUp() {
        // 设置测试配置
        articleConfig = new RabbitMQProperties.ArticleConfig();
        articleConfig.setExchange("test.article.exchange");
        articleConfig.setQueue("test.article.queue");
        articleConfig.setRoutingKey("test.article.publish");
        articleConfig.setSearchQueue("test.article.search.queue");
        articleConfig.setSearchRoutingKey("test.article.search");
        articleConfig.setCacheQueue("test.article.cache.queue");
        articleConfig.setCacheRoutingKey("test.article.cache");
        articleConfig.setTagQueue("test.article.tag.queue");
        articleConfig.setTagRoutingKey("test.article.tag");

        deadLetterConfig = new RabbitMQProperties.DeadLetterConfig();
        deadLetterConfig.setExchange("test.dlx.exchange");
        deadLetterConfig.setQueue("test.dlx.queue");
        deadLetterConfig.setRoutingKey("test.dlx");

        // 移除不必要的 stubbing，在需要时单独设置
    }

    @Test
    void testSendArticlePublishMessage_Success() {
        // 准备测试数据
        when(rabbitMQProperties.getArticle()).thenReturn(articleConfig);
        ArticlePublishMessage message = new ArticlePublishMessage();
        message.setArticleId(1);
        message.setUserId(1L);
        message.setTitle("测试文章");
        message.setContent("测试内容");

        // 执行测试
        messageProducerService.sendArticlePublishMessage(message);

        // 验证结果
        verify(rabbitTemplate).convertAndSend(
            "test.article.exchange",
            "test.article.publish",
            message
        );
        verify(metricsCollector).recordMessageSent("ARTICLE_PUBLISH", true);
    }

    @Test
    void testSendSearchSyncMessage_Success() {
        // 准备测试数据
        when(rabbitMQProperties.getArticle()).thenReturn(articleConfig);
        ArticleSearchSyncMessage message = ArticleSearchSyncMessage.createIndexMessage(1, "test_business_id");
        message.setTitle("测试文章");
        message.setContent("测试内容");

        // 执行测试
        messageProducerService.sendSearchSyncMessage(message);

        // 验证结果
        verify(rabbitTemplate).convertAndSend(
            "test.article.exchange",
            "test.article.search",
            message
        );
        verify(metricsCollector).recordMessageSent("SEARCH_SYNC", true);
    }

    @Test
    void testSendCacheUpdateMessage_Success() {
        // 准备测试数据
        when(rabbitMQProperties.getArticle()).thenReturn(articleConfig);
        ArticleCacheUpdateMessage message = ArticleCacheUpdateMessage.createUpdateCounterMessage(
            1, "test_business_id", 
            java.util.List.of(ArticleCacheUpdateMessage.CounterType.USER_ARTICLE_COUNT)
        );

        // 执行测试
        messageProducerService.sendCacheUpdateMessage(message);

        // 验证结果
        verify(rabbitTemplate).convertAndSend(
            "test.article.exchange",
            "test.article.cache",
            message
        );
        verify(metricsCollector).recordMessageSent("CACHE_UPDATE", true);
    }

    @Test
    void testSendTagSyncMessage_Success() {
        // 准备测试数据
        when(rabbitMQProperties.getArticle()).thenReturn(articleConfig);
        ArticleTagSyncMessage message = ArticleTagSyncMessage.createTagProcessMessage(
            1, "test_business_id", java.util.List.of("Java", "Spring")
        );

        // 执行测试
        messageProducerService.sendTagSyncMessage(message);

        // 验证结果
        verify(rabbitTemplate).convertAndSend(
            "test.article.exchange",
            "test.article.tag",
            message
        );
        verify(metricsCollector).recordMessageSent("TAG_SYNC", true);
    }

    @Test
    void testSendToDeadLetterQueue_Success() {
        // 准备测试数据
        when(rabbitMQProperties.getDeadLetter()).thenReturn(deadLetterConfig);
        ArticlePublishMessage message = new ArticlePublishMessage();
        message.setMessageId("test-message-id");
        message.setMessageType("ARTICLE_PUBLISH");

        // 执行测试
        messageProducerService.sendToDeadLetterQueue(message);

        // 验证结果
        verify(rabbitTemplate).convertAndSend(
            "test.dlx.exchange",
            "test.dlx",
            message
        );
        verify(metricsCollector).recordMessageDeadLettered("ARTICLE_PUBLISH");
    }

    @Test
    void testIsConnectionHealthy_True() {
        // 准备测试数据
        when(rabbitMQProperties.getArticle()).thenReturn(articleConfig);
        
        // 执行测试
        boolean result = messageProducerService.isConnectionHealthy();

        // 验证结果
        assertTrue(result);
        verify(rabbitTemplate).convertAndSend(
            "test.article.exchange",
            "test.article.publish",
            "health_check"
        );
    }

    @Test
    void testIsConnectionHealthy_False() {
        // 准备测试数据
        when(rabbitMQProperties.getArticle()).thenReturn(articleConfig);
        
        // 模拟异常
        doThrow(new RuntimeException("Connection failed"))
            .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // 执行测试
        boolean result = messageProducerService.isConnectionHealthy();

        // 验证结果
        assert !result;
    }
}
