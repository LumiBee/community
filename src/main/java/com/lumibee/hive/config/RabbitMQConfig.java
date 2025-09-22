package com.lumibee.hive.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 
 * 配置 RabbitMQ 连接、交换机、队列、消息转换器等
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    @Autowired
    private RabbitMQProperties rabbitMQProperties;

    /**
     * 配置连接工厂
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitMQProperties.getHost());
        connectionFactory.setPort(rabbitMQProperties.getPort());
        connectionFactory.setUsername(rabbitMQProperties.getUsername());
        connectionFactory.setPassword(rabbitMQProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitMQProperties.getVirtualHost());
        connectionFactory.setConnectionTimeout(rabbitMQProperties.getConnectionTimeout());
        connectionFactory.setRequestedHeartBeat(rabbitMQProperties.getRequestedHeartbeat());
        
        // 开启发布确认
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);
        
        log.info("RabbitMQ 连接配置完成: {}:{}", rabbitMQProperties.getHost(), rabbitMQProperties.getPort());
        return connectionFactory;
    }

    /**
     * 配置消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        
        // 开启发布确认
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送成功: {}", correlationData);
            } else {
                log.error("消息发送失败: {}, 原因: {}", correlationData, cause);
            }
        });
        
        // 开启发布返回
        template.setReturnsCallback(returned -> {
            log.error("消息被退回: {}, 退回原因: {}, 交换机: {}, 路由键: {}", 
                returned.getMessage(), returned.getReplyText(), 
                returned.getExchange(), returned.getRoutingKey());
        });
        
        return template;
    }

    /**
     * 配置监听器容器工厂
     */
    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(rabbitMQProperties.getPrefetchCount());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }

    // ==================== 死信队列配置 ====================

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(rabbitMQProperties.getDeadLetter().getExchange())
                .durable(true)
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getDeadLetter().getQueue())
                .build();
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(rabbitMQProperties.getDeadLetter().getRoutingKey());
    }

    // ==================== 文章发布相关配置 ====================

    /**
     * 文章发布交换机
     */
    @Bean
    public DirectExchange articleExchange() {
        return ExchangeBuilder.directExchange(rabbitMQProperties.getArticle().getExchange())
                .durable(true)
                .build();
    }

    /**
     * 文章发布主队列
     */
    @Bean
    public Queue articleQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getArticle().getQueue())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getDeadLetter().getExchange())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getDeadLetter().getRoutingKey())
                .build();
    }

    /**
     * 文章发布队列绑定
     */
    @Bean
    public Binding articleBinding() {
        return BindingBuilder.bind(articleQueue())
                .to(articleExchange())
                .with(rabbitMQProperties.getArticle().getRoutingKey());
    }

    /**
     * 搜索同步队列
     */
    @Bean
    public Queue articleSearchQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getArticle().getSearchQueue())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getDeadLetter().getExchange())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getDeadLetter().getRoutingKey())
                .build();
    }

    /**
     * 搜索同步队列绑定
     */
    @Bean
    public Binding articleSearchBinding() {
        return BindingBuilder.bind(articleSearchQueue())
                .to(articleExchange())
                .with(rabbitMQProperties.getArticle().getSearchRoutingKey());
    }

    /**
     * 缓存更新队列
     */
    @Bean
    public Queue articleCacheQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getArticle().getCacheQueue())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getDeadLetter().getExchange())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getDeadLetter().getRoutingKey())
                .build();
    }

    /**
     * 缓存更新队列绑定
     */
    @Bean
    public Binding articleCacheBinding() {
        return BindingBuilder.bind(articleCacheQueue())
                .to(articleExchange())
                .with(rabbitMQProperties.getArticle().getCacheRoutingKey());
    }

    /**
     * 标签同步队列
     */
    @Bean
    public Queue articleTagQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getArticle().getTagQueue())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getDeadLetter().getExchange())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getDeadLetter().getRoutingKey())
                .build();
    }

    /**
     * 标签同步队列绑定
     */
    @Bean
    public Binding articleTagBinding() {
        return BindingBuilder.bind(articleTagQueue())
                .to(articleExchange())
                .with(rabbitMQProperties.getArticle().getTagRoutingKey());
    }
}
