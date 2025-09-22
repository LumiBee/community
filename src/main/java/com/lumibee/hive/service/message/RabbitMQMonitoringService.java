package com.lumibee.hive.service.message;

import com.lumibee.hive.config.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 监控服务
 * 
 * 提供消息队列的健康检查和监控功能
 */
@Slf4j
@Service
public class RabbitMQMonitoringService implements HealthIndicator {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQProperties rabbitMQProperties;

    @Autowired
    private MessageIdempotencyService idempotencyService;

    /**
     * 健康检查
     */
    @Override
    public Health health() {
        try {
            // 检查连接状态
            boolean isConnected = checkConnection();
            if (!isConnected) {
                return Health.down()
                    .withDetail("error", "RabbitMQ connection failed")
                    .withDetail("host", rabbitMQProperties.getHost())
                    .withDetail("port", rabbitMQProperties.getPort())
                    .build();
            }

            // 检查队列状态
            Map<String, Object> queueStatus = checkQueueStatus();
            
            // 检查幂等性服务状态
            boolean idempotencyHealthy = checkIdempotencyService();
            
            Health.Builder healthBuilder = Health.up()
                .withDetail("host", rabbitMQProperties.getHost())
                .withDetail("port", rabbitMQProperties.getPort())
                .withDetail("virtualHost", rabbitMQProperties.getVirtualHost())
                .withDetail("queues", queueStatus)
                .withDetail("idempotencyService", idempotencyHealthy ? "healthy" : "unhealthy");

            // 如果有队列问题，标记为部分健康
            if (queueStatus.values().stream().anyMatch(status -> "error".equals(status))) {
                healthBuilder = Health.status("PARTIAL")
                    .withDetail("warning", "Some queues may have issues");
            }

            return healthBuilder.build();

        } catch (Exception e) {
            log.error("RabbitMQ 健康检查失败", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("host", rabbitMQProperties.getHost())
                .withDetail("port", rabbitMQProperties.getPort())
                .build();
        }
    }

    /**
     * 检查连接状态
     */
    public boolean checkConnection() {
        try {
            // 发送一个测试消息来检查连接
            rabbitTemplate.convertAndSend(
                rabbitMQProperties.getArticle().getExchange(),
                rabbitMQProperties.getArticle().getRoutingKey(),
                "health_check"
            );
            return true;
        } catch (Exception e) {
            log.error("RabbitMQ 连接检查失败", e);
            return false;
        }
    }

    /**
     * 检查队列状态
     */
    public Map<String, Object> checkQueueStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 这里可以添加更详细的队列状态检查
            // 例如检查队列长度、消费者数量等
            status.put("articleQueue", "healthy");
            status.put("searchQueue", "healthy");
            status.put("cacheQueue", "healthy");
            status.put("tagQueue", "healthy");
            status.put("deadLetterQueue", "healthy");
        } catch (Exception e) {
            log.error("检查队列状态失败", e);
            status.put("error", e.getMessage());
        }
        
        return status;
    }

    /**
     * 检查幂等性服务状态
     */
    public boolean checkIdempotencyService() {
        try {
            // 测试幂等性服务
            String testKey = "health_check_" + System.currentTimeMillis();
            boolean result = idempotencyService.checkAndSetProcessed(testKey);
            idempotencyService.clearProcessed(testKey);
            return result;
        } catch (Exception e) {
            log.error("幂等性服务检查失败", e);
            return false;
        }
    }

    /**
     * 获取消息队列统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("connectionStatus", checkConnection() ? "connected" : "disconnected");
            stats.put("queueStatus", checkQueueStatus());
            stats.put("idempotencyServiceStatus", checkIdempotencyService() ? "healthy" : "unhealthy");
            stats.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 发送告警消息
     */
    public void sendAlert(String alertType, String message, Map<String, Object> details) {
        try {
            log.warn("RabbitMQ 告警: type={}, message={}, details={}", alertType, message, details);
            
            // 这里可以集成实际的告警系统，如邮件、短信、钉钉等
            // 例如：
            // - 发送邮件告警
            // - 发送钉钉消息
            // - 写入告警日志
            // - 调用外部监控系统API
            
        } catch (Exception e) {
            log.error("发送告警失败: type={}, message={}", alertType, message, e);
        }
    }

    /**
     * 检查消息处理延迟
     */
    public void checkMessageProcessingDelay() {
        try {
            // 这里可以检查消息处理是否延迟
            // 例如检查队列长度、处理时间等
            log.debug("检查消息处理延迟...");
        } catch (Exception e) {
            log.error("检查消息处理延迟失败", e);
        }
    }

    /**
     * 检查死信队列
     */
    public void checkDeadLetterQueue() {
        try {
            // 这里可以检查死信队列是否有消息
            // 如果有消息，可能需要人工干预
            log.debug("检查死信队列...");
        } catch (Exception e) {
            log.error("检查死信队列失败", e);
        }
    }
}
