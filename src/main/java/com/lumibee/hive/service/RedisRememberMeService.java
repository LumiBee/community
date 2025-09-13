package com.lumibee.hive.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.lumibee.hive.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class RedisRememberMeService {
    
    private static final String REMEMBER_ME_PREFIX = "REMEMBER_ME:";
    private static final Duration REMEMBER_ME_TTL = Duration.ofDays(14);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    
    /**
     * 创建remember-me token
     */
    public String createRememberMeToken(User user) {
        String token = UUID.randomUUID().toString();
        String key = REMEMBER_ME_PREFIX + token;

        redisTemplate.opsForValue().set(key, user, REMEMBER_ME_TTL);
        return token;
    }
    
    /**
     * 验证remember-me token
     */
    public User validateRememberMeToken(String token) {
        String key = REMEMBER_ME_PREFIX + token;
        return (User) redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 设置remember-me cookie
     */
    public void setRememberMeCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REMEMBER_ME_PREFIX, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) REMEMBER_ME_TTL.getSeconds());
        response.addCookie(cookie);
    }

    /**
     * 从请求中获取remember-me token
     */
    public String getRememberMeTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REMEMBER_ME_PREFIX.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * 删除remember-me token
     */
    public void removeRememberMeToken(String token) {
        String key = REMEMBER_ME_PREFIX + token;
        redisTemplate.delete(key);
    }

}
