package com.lumibee.hive.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(Environment environment) {
        String password = environment.getProperty("DB_PASSWORD");

        return DataSourceBuilder.create()
                .url("jdbc:mysql://mysql-db:3306/community?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true")
                .username("root")
                .password(password)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}