package com.lumibee.hive.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.lumibee.hive.model.User;

@Service
public class RedisSessionService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_PREFIX = "session:";
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);
    
    // 存储会话
    public void storeSession(String sessionId, User user) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, user, SESSION_TIMEOUT);
    }

    // 获取会话
    public User getSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return (User) redisTemplate.opsForValue().get(key);
    }

    // 删除会话
    public void deleteSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }

    // 检查会话是否存在
    public boolean hasSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return redisTemplate.hasKey(key);
    }

    // 更新会话
    public void refreshSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.expire(key, SESSION_TIMEOUT);
    }
}
