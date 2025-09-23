package com.lumibee.hive.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.log4j.Log4j2;

/**
 * Redisson 配置类
 * 
 * 功能：
 * 1. 配置 Redisson 客户端
 * 2. 提供分布式锁支持
 * 3. 防止缓存击穿、缓存雪崩等问题
 * 
 * 配置说明：
 * - 使用单节点模式
 * - 配置连接池参数
 * - 设置锁监控超时时间
 * - 优化网络参数
 */
@Configuration
@Log4j2
public class RedissonConfig {

    @Value("${spring.redis.redisson.config}")
    private String redissonConfig;

    /**
     * 创建 Redisson 客户端
     * 使用 Spring Boot 自动配置，这里提供手动配置作为备选
     */
    @Bean
    public RedissonClient redissonClient() {
        try {
            // 使用配置文件中的 Redisson 配置
            Config config = Config.fromYAML(redissonConfig);
            log.info("Redisson 客户端配置完成");
            return Redisson.create(config);
        } catch (Exception e) {
            log.error("创建 Redisson 客户端失败", e);
            throw new RuntimeException("Redisson 客户端初始化失败", e);
        }
    }
}
