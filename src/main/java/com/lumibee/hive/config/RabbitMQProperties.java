package com.lumibee.hive.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 配置属性类
 * 
 * 用于管理 RabbitMQ 相关的配置参数，支持多环境配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {

    /**
     * RabbitMQ 服务器地址
     */
    private String host = "localhost";

    /**
     * RabbitMQ 端口
     */
    private int port = 5672;

    /**
     * 用户名
     */
    private String username = "admin";

    /**
     * 密码
     */
    private String password = "admin";

    /**
     * 虚拟主机
     */
    private String virtualHost = "/";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectionTimeout = 30000;

    /**
     * 请求心跳间隔（秒）
     */
    private int requestedHeartbeat = 60;

    /**
     * 发布确认超时时间（毫秒）
     */
    private int publisherConfirmTimeout = 5000;

    /**
     * 发布返回超时时间（毫秒）
     */
    private int publisherReturnsTimeout = 5000;

    /**
     * 消费者确认模式
     * manual: 手动确认
     * auto: 自动确认
     * none: 不确认
     */
    private String ackMode = "manual";

    /**
     * 预取数量
     */
    private int prefetchCount = 1;

    /**
     * 重试次数
     */
    private int maxRetryAttempts = 3;

    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval = 1000;

    /**
     * 死信队列配置
     */
    private DeadLetterConfig deadLetter = new DeadLetterConfig();

    @Data
    public static class DeadLetterConfig {
        /**
         * 死信交换机名称
         */
        private String exchange = "hive.dlx.exchange";

        /**
         * 死信队列名称
         */
        private String queue = "hive.dlx.queue";

        /**
         * 死信路由键
         */
        private String routingKey = "hive.dlx";
    }

    /**
     * 文章发布相关配置
     */
    private ArticleConfig article = new ArticleConfig();

    @Data
    public static class ArticleConfig {
        /**
         * 文章发布交换机名称
         */
        private String exchange = "hive.article.exchange";

        /**
         * 文章发布队列名称
         */
        private String queue = "hive.article.queue";

        /**
         * 文章发布路由键
         */
        private String routingKey = "hive.article.publish";

        /**
         * 搜索同步队列名称
         */
        private String searchQueue = "hive.article.search.queue";

        /**
         * 搜索同步路由键
         */
        private String searchRoutingKey = "hive.article.search";

        /**
         * 缓存更新队列名称
         */
        private String cacheQueue = "hive.article.cache.queue";

        /**
         * 缓存更新路由键
         */
        private String cacheRoutingKey = "hive.article.cache";

        /**
         * 标签同步队列名称
         */
        private String tagQueue = "hive.article.tag.queue";

        /**
         * 标签同步路由键
         */
        private String tagRoutingKey = "hive.article.tag";
    }
}
