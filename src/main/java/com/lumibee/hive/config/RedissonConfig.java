package com.lumibee.hive.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class RedissonConfig {

    @Value("${spring.redis.redisson.config}")
    private String redissonConfig;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private String redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        try {
            Config config = Config.fromYAML(redissonConfig);

            String address = "redis://" + redisHost + ":" + redisPort;
            config.useSingleServer().setAddress(address);

            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.useSingleServer().setPassword(redisPassword);
            }

            log.info("Redisson 连接地址已修正为: {}", address);

            return Redisson.create(config);
        } catch (Exception e) {
            log.error("创建 Redisson 客户端失败", e);
            throw new RuntimeException("Redisson 客户端初始化失败", e);
        }
    }
}