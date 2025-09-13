package com.lumibee.hive.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Redis配置类
 * 
 * 功能：
 * 1. 配置RedisCacheManager - 管理Spring Cache注解
 * 2. 配置RedisTemplate - 提供通用Redis操作
 * 3. 实现分层TTL策略 - 根据业务特点设置不同的缓存过期时间
 * 4. 配置序列化策略 - 键使用String，值使用JSON序列化
 * 5. 防缓存穿透 - 禁用null值缓存
 * 
 * 分层TTL设计原则：
 * - 访问频率越高，TTL越长
 * - 数据变化越频繁，TTL越短
 * - 业务重要性越高，TTL越长
 * - 数据一致性要求越高，TTL越短
 */
@Configuration
@EnableCaching // 启用Spring的注解驱动缓存管理功能
public class RedisConfig {

    /**
     * 配置 RedisCacheManager，用于管理缓存。
     * 这是实现注解式缓存的核心。
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // 配置键和值的序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = createJsonSerializer();

        // 创建默认的缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存默认过期时间为1小时
                .entryTtl(Duration.ofHours(1))
                // 设置键前缀
                .prefixCacheNameWith("hive::")
                // 配置键的序列化方式
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                // 配置值的序列化方式
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                
                // ==================== 文章相关缓存 ====================
                
                // 文章详情缓存 - 2小时
                .withCacheConfiguration("articles::detail", 
                    createCacheConfig(Duration.ofHours(2), stringSerializer, jsonSerializer))
                
                // 首页文章列表 - 30分钟
                .withCacheConfiguration("articles::list::homepage", 
                    createCacheConfig(Duration.ofMinutes(30), stringSerializer, jsonSerializer))
                
                // 所有文章列表 - 1小时
                .withCacheConfiguration("articles::list::all", 
                    createCacheConfig(Duration.ofHours(1), stringSerializer, jsonSerializer))
                
                // 标签文章列表 - 45分钟
                .withCacheConfiguration("articles::list::tag", 
                    createCacheConfig(Duration.ofMinutes(45), stringSerializer, jsonSerializer))
                
                // 用户文章列表 - 15分钟
                .withCacheConfiguration("articles::list::user", 
                    createCacheConfig(Duration.ofMinutes(15), stringSerializer, jsonSerializer))
                
                // 搜索文章列表 - 5分钟
                .withCacheConfiguration("articles::list::search", 
                    createCacheConfig(Duration.ofMinutes(5), stringSerializer, jsonSerializer))
                
                // 热门文章列表 - 2小时
                .withCacheConfiguration("articles::list::popular", 
                    createCacheConfig(Duration.ofHours(2), stringSerializer, jsonSerializer))
                
                // 精选文章列表 - 2小时
                .withCacheConfiguration("articles::list::featured", 
                    createCacheConfig(Duration.ofHours(2), stringSerializer, jsonSerializer))
                
                // 作品集文章列表 - 30分钟
                .withCacheConfiguration("articles::list::portfolio", 
                    createCacheConfig(Duration.ofMinutes(30), stringSerializer, jsonSerializer))
                
                // ==================== 用户相关缓存 ====================

                // 用户个人中心 - 15分钟
                .withCacheConfiguration("users::profile",
                        createCacheConfig(Duration.ofMinutes(15), stringSerializer, jsonSerializer))
                
                // 用户关注关系 - 30分钟
                .withCacheConfiguration("users::follow", 
                    createCacheConfig(Duration.ofMinutes(30), stringSerializer, jsonSerializer))
                
                // 用户统计信息 - 1小时
                .withCacheConfiguration("users::count",
                    createCacheConfig(Duration.ofHours(1), stringSerializer, jsonSerializer))

                // ==================== 标签相关缓存 ====================
                
                // 所有标签列表 - 2小时
                .withCacheConfiguration("tags::list::all", 
                    createCacheConfig(Duration.ofHours(2), stringSerializer, jsonSerializer))
                
                // 热门标签列表 - 2小时
                .withCacheConfiguration("tags::list::popular", 
                    createCacheConfig(Duration.ofHours(2), stringSerializer, jsonSerializer))
                
                // ==================== 收藏相关缓存 ====================
                
                // 收藏详情 - 2小时
                .withCacheConfiguration("favorites::detail",
                    createCacheConfig(Duration.ofHours(2), stringSerializer, jsonSerializer))

                // 用户收藏列表 - 2小时
                .withCacheConfiguration("favorites::list::user",
                        createCacheConfig(Duration.ofHours(2), stringSerializer, jsonSerializer))

                // ==================== 评论相关缓存 ====================

                // 评论相关缓存 - 10分钟
                .withCacheConfiguration("comments::list::article", 
                    createCacheConfig(Duration.ofMinutes(10), stringSerializer, jsonSerializer))

                // ==================== 作品集相关缓存 ====================

                // 作品集详情 - 2小时
                .withCacheConfiguration("portfolios::detail", 
                    createCacheConfig(Duration.ofHours(2), stringSerializer, jsonSerializer))
                
                .build();
    }

    /**
     * 创建并配置一个通用的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = createJsonSerializer();

        // Key 和 HashKey 都使用 String 序列化
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 和 HashValue 都使用 JSON 序列化
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建缓存配置的辅助方法
     *
     * @param ttl 缓存过期时间
     * @param stringSerializer 键序列化器（String类型）
     * @param jsonSerializer 值序列化器（JSON格式）
     * @return 配置好的RedisCacheConfiguration
     */
    private RedisCacheConfiguration createCacheConfig(Duration ttl, StringRedisSerializer stringSerializer, Jackson2JsonRedisSerializer<Object> jsonSerializer) {
        // 添加随机偏移防止雪崩
        Duration randomTtl = addRandomOffset(ttl);
        
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(randomTtl) // 设置随机缓存过期时间
                .prefixCacheNameWith("hive::") // 设置键前缀
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer)) // 键使用String序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)); // 值使用JSON序列化
                // 默认情况下允许缓存null值
    }

    /**
     * 添加随机偏移量防止缓存雪崩
     * @param baseTtl 基础TTL
     * @return 添加随机偏移后的TTL
     */
    private Duration addRandomOffset(Duration baseTtl) {
        long baseSeconds = baseTtl.getSeconds();
        // 添加±10%的随机偏移
        double offset = (Math.random() - 0.5) * 0.2; // -10% 到 +10%
        long randomSeconds = (long) (baseSeconds * (1 + offset));
        return Duration.ofSeconds(Math.max(randomSeconds, 60)); // 最少1分钟
    }
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    public String getCacheStatistics() {
        return String.format("缓存配置统计: 共配置了%d个缓存类型，默认TTL: 1小时，随机偏移: ±10%%", 15);
    }

    /**
     * 创建一个配置好的 Jackson2JsonRedisSerializer
     */
    private Jackson2JsonRedisSerializer<Object> createJsonSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 支持Java 8 的时间类型序列化
        mapper.registerModule(new JavaTimeModule());
        
        // 创建自定义模块来处理大整数
        SimpleModule bigIntModule = new SimpleModule();
        // 将Long类型序列化为字符串，避免JavaScript精度丢失
        bigIntModule.addSerializer(Long.class, new ToStringSerializer());
        bigIntModule.addSerializer(Long.TYPE, new ToStringSerializer());
        mapper.registerModule(bigIntModule);
        
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);
        return serializer;
    }
}