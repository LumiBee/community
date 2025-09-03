package com.lumibee.hive.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lumibee.hive.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class RememberMeService {
    
    private static final String REMEMBER_ME_COOKIE_NAME = "remember-me";
    private static final int REMEMBER_ME_DURATION = 1209600; // 2周，单位：秒
    
    // 内存存储remember-me token，实际应用中应该使用数据库
    private static final Map<String, RememberMeToken> tokenStore = new ConcurrentHashMap<>();
    
    @Autowired
    private UserService userService;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 创建remember-me token
     */
    public String createRememberMeToken(User user) {
        // 生成随机token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        // 创建token对象
        RememberMeToken rememberMeToken = new RememberMeToken();
        rememberMeToken.setUsername(user.getName());
        rememberMeToken.setUserId(user.getId());
        rememberMeToken.setCreatedAt(System.currentTimeMillis());
        rememberMeToken.setExpiresAt(System.currentTimeMillis() + (REMEMBER_ME_DURATION * 1000L));
        
        // 存储token
        tokenStore.put(token, rememberMeToken);
        
        return token;
    }
    
    /**
     * 验证remember-me token
     */
    public User validateRememberMeToken(String token) {
        RememberMeToken rememberMeToken = tokenStore.get(token);
        
        if (rememberMeToken == null) {
            return null;
        }
        
        // 检查token是否过期
        if (System.currentTimeMillis() > rememberMeToken.getExpiresAt()) {
            tokenStore.remove(token);
            return null;
        }
        
        // 获取用户信息
        try {
            return userService.selectById(rememberMeToken.getUserId());
        } catch (Exception e) {
            // 如果获取用户失败，删除token
            tokenStore.remove(token);
            return null;
        }
    }
    
    /**
     * 设置remember-me cookie
     */
    public void setRememberMeCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, token);
        cookie.setMaxAge(REMEMBER_ME_DURATION);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 在生产环境中应该设置为true（HTTPS）
        response.addCookie(cookie);
    }
    
    /**
     * 清除remember-me cookie
     */
    public void clearRememberMeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    
    /**
     * 从请求中获取remember-me token
     */
    public String getRememberMeTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REMEMBER_ME_COOKIE_NAME.equals(cookie.getName())) {
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
        tokenStore.remove(token);
    }
    
    /**
     * 清理过期的token
     */
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        tokenStore.entrySet().removeIf(entry -> entry.getValue().getExpiresAt() < currentTime);
    }
    
    /**
     * RememberMeToken内部类
     */
    public static class RememberMeToken {
        private String username;
        private Long userId;
        private long createdAt;
        private long expiresAt;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
        
        public long getExpiresAt() { return expiresAt; }
        public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    }
}
