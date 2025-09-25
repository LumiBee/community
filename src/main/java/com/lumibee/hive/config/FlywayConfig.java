package com.lumibee.hive.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Flyway 数据库迁移配置类
 * 处理DevTools自动重启导致的重复迁移问题
 * 
 * @author lumibee
 */
@Configuration
public class FlywayConfig {

    @Autowired
    private Environment environment;

    /**
     * 自定义Flyway迁移策略
     * 在开发环境避免重复执行迁移
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayMigrationStrategy() {
            @Override
            public void migrate(Flyway flyway) {
                String activeProfile = environment.getActiveProfiles().length > 0 
                    ? environment.getActiveProfiles()[0] 
                    : "dev";
                
                if ("dev".equals(activeProfile)) {
                    // 开发环境：检查是否已经执行过迁移，避免重复执行
                    try {
                        // 先验证，如果验证失败才执行迁移
                        flyway.validate();
                        // 如果验证通过，说明迁移已经是最新的，不需要重复执行
                    } catch (Exception e) {
                        // 验证失败，执行迁移
                        flyway.migrate();
                    }
                } else {
                    // 生产环境：正常执行迁移
                    flyway.migrate();
                }
            }
        };
    }
}
