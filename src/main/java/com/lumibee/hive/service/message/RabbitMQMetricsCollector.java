package com.lumibee.hive.service.message;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RabbitMQ 指标收集器
 * 
 * 收集和暴露消息队列相关的监控指标
 */
@Slf4j
@Service
public class RabbitMQMetricsCollector {

    @Autowired
    private MeterRegistry meterRegistry;


    // 消息发送指标
    private Counter messagesSentTotal;
    private Counter messagesSentSuccess;
    private Counter messagesSentFailure;

    // 消息消费指标
    private Counter messagesConsumedTotal;
    private Counter messagesConsumedSuccess;
    private Counter messagesConsumedFailure;

    // 消息重试指标
    private Counter messagesRetriedTotal;
    private Counter messagesDeadLetteredTotal;

    // 幂等性检查指标
    private Counter idempotencyCheckTotal;
    private Counter idempotencyCheckHit;
    private Counter idempotencyCheckMiss;

    // 处理时间指标（在方法中动态创建）

    // 队列长度指标（模拟值，实际应该从 RabbitMQ 管理 API 获取）
    private AtomicLong articleQueueLength = new AtomicLong(0);
    private AtomicLong searchQueueLength = new AtomicLong(0);
    private AtomicLong cacheQueueLength = new AtomicLong(0);
    private AtomicLong tagQueueLength = new AtomicLong(0);
    private AtomicLong deadLetterQueueLength = new AtomicLong(0);

    @PostConstruct
    public void initMetrics() {
        // 初始化计数器
        messagesSentTotal = Counter.builder("rabbitmq.messages.sent.total")
                .description("Total number of messages sent")
                .register(meterRegistry);

        messagesSentSuccess = Counter.builder("rabbitmq.messages.sent.success")
                .description("Number of messages sent successfully")
                .register(meterRegistry);

        messagesSentFailure = Counter.builder("rabbitmq.messages.sent.failure")
                .description("Number of messages failed to send")
                .register(meterRegistry);

        messagesConsumedTotal = Counter.builder("rabbitmq.messages.consumed.total")
                .description("Total number of messages consumed")
                .register(meterRegistry);

        messagesConsumedSuccess = Counter.builder("rabbitmq.messages.consumed.success")
                .description("Number of messages consumed successfully")
                .register(meterRegistry);

        messagesConsumedFailure = Counter.builder("rabbitmq.messages.consumed.failure")
                .description("Number of messages failed to consume")
                .register(meterRegistry);

        messagesRetriedTotal = Counter.builder("rabbitmq.messages.retried.total")
                .description("Total number of messages retried")
                .register(meterRegistry);

        messagesDeadLetteredTotal = Counter.builder("rabbitmq.messages.dead_lettered.total")
                .description("Total number of messages sent to dead letter queue")
                .register(meterRegistry);

        idempotencyCheckTotal = Counter.builder("rabbitmq.idempotency.check.total")
                .description("Total number of idempotency checks")
                .register(meterRegistry);

        idempotencyCheckHit = Counter.builder("rabbitmq.idempotency.check.hit")
                .description("Number of idempotency check hits (duplicate messages)")
                .register(meterRegistry);

        idempotencyCheckMiss = Counter.builder("rabbitmq.idempotency.check.miss")
                .description("Number of idempotency check misses (new messages)")
                .register(meterRegistry);

        // 计时器在方法中动态创建

        // 初始化队列长度指标
        Gauge.builder("rabbitmq.queue.length", articleQueueLength, AtomicLong::get)
                .description("Current queue length")
                .tag("queue", "article")
                .register(meterRegistry);

        Gauge.builder("rabbitmq.queue.length", searchQueueLength, AtomicLong::get)
                .description("Current queue length")
                .tag("queue", "search")
                .register(meterRegistry);

        Gauge.builder("rabbitmq.queue.length", cacheQueueLength, AtomicLong::get)
                .description("Current queue length")
                .tag("queue", "cache")
                .register(meterRegistry);

        Gauge.builder("rabbitmq.queue.length", tagQueueLength, AtomicLong::get)
                .description("Current queue length")
                .tag("queue", "tag")
                .register(meterRegistry);

        Gauge.builder("rabbitmq.queue.length", deadLetterQueueLength, AtomicLong::get)
                .description("Current queue length")
                .tag("queue", "dead_letter")
                .register(meterRegistry);

        log.info("RabbitMQ 指标收集器初始化完成");
    }

    /**
     * 记录消息发送
     */
    public void recordMessageSent(String messageType, boolean success) {
        messagesSentTotal.increment();
        if (success) {
            messagesSentSuccess.increment();
        } else {
            messagesSentFailure.increment();
        }
    }

    /**
     * 记录消息消费
     */
    public void recordMessageConsumed(String messageType, boolean success) {
        messagesConsumedTotal.increment();
        if (success) {
            messagesConsumedSuccess.increment();
        } else {
            messagesConsumedFailure.increment();
        }
    }

    /**
     * 记录消息重试
     */
    public void recordMessageRetried(String messageType) {
        messagesRetriedTotal.increment();
    }

    /**
     * 记录消息进入死信队列
     */
    public void recordMessageDeadLettered(String messageType) {
        messagesDeadLetteredTotal.increment();
    }

    /**
     * 记录幂等性检查
     */
    public void recordIdempotencyCheck(boolean hit) {
        idempotencyCheckTotal.increment();
        if (hit) {
            idempotencyCheckHit.increment();
        } else {
            idempotencyCheckMiss.increment();
        }
    }

    /**
     * 记录消息处理时间
     */
    public Timer.Sample startMessageProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 停止消息处理计时器
     */
    public void stopMessageProcessingTimer(Timer.Sample sample, String messageType) {
        sample.stop(Timer.builder("rabbitmq.message.processing.time")
                .tag("message_type", messageType)
                .register(meterRegistry));
    }

    /**
     * 记录消息发送时间
     */
    public Timer.Sample startMessageSendTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 停止消息发送计时器
     */
    public void stopMessageSendTimer(Timer.Sample sample, String messageType) {
        sample.stop(Timer.builder("rabbitmq.message.send.time")
                .tag("message_type", messageType)
                .register(meterRegistry));
    }

    /**
     * 更新队列长度（模拟值）
     */
    public void updateQueueLength(String queueName, long length) {
        switch (queueName) {
            case "article":
                articleQueueLength.set(length);
                break;
            case "search":
                searchQueueLength.set(length);
                break;
            case "cache":
                cacheQueueLength.set(length);
                break;
            case "tag":
                tagQueueLength.set(length);
                break;
            case "dead_letter":
                deadLetterQueueLength.set(length);
                break;
            default:
                log.warn("未知的队列名称: {}", queueName);
        }
    }

    /**
     * 获取当前指标摘要
     */
    public String getMetricsSummary() {
        return String.format(
            "RabbitMQ Metrics Summary:\n" +
            "Messages Sent: %d (Success: %d, Failure: %d)\n" +
            "Messages Consumed: %d (Success: %d, Failure: %d)\n" +
            "Messages Retried: %d\n" +
            "Messages Dead Lettered: %d\n" +
            "Idempotency Checks: %d (Hit: %d, Miss: %d)\n" +
            "Queue Lengths - Article: %d, Search: %d, Cache: %d, Tag: %d, Dead Letter: %d",
            (int) messagesSentTotal.count(),
            (int) messagesSentSuccess.count(),
            (int) messagesSentFailure.count(),
            (int) messagesConsumedTotal.count(),
            (int) messagesConsumedSuccess.count(),
            (int) messagesConsumedFailure.count(),
            (int) messagesRetriedTotal.count(),
            (int) messagesDeadLetteredTotal.count(),
            (int) idempotencyCheckTotal.count(),
            (int) idempotencyCheckHit.count(),
            (int) idempotencyCheckMiss.count(),
            articleQueueLength.get(),
            searchQueueLength.get(),
            cacheQueueLength.get(),
            tagQueueLength.get(),
            deadLetterQueueLength.get()
        );
    }
}
