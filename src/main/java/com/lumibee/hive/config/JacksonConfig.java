package com.lumibee.hive.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson配置类
 * 解决大整数ID在JSON序列化时精度丢失的问题
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 注册Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        
        // 创建自定义模块来处理大整数
        SimpleModule bigIntModule = new SimpleModule();
        
        // 将Long类型序列化为字符串，避免JavaScript精度丢失
        bigIntModule.addSerializer(Long.class, new ToStringSerializer());
        bigIntModule.addSerializer(Long.TYPE, new ToStringSerializer());
        
        // 注册自定义模块
        objectMapper.registerModule(bigIntModule);
        
        return objectMapper;
    }
}
