package com.lumibee.hive.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.RedisSessionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RedisSessionFilter extends OncePerRequestFilter {
    
    @Autowired
    private RedisSessionService redisSessionService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // 检查Redis会话
            String sessionId = getSessionIdFromRequest(request);
            if (sessionId != null) {
                User user = redisSessionService.getSession(sessionId);
                if (user != null) {
                    // 设置到SecurityContext
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    
                    // 刷新会话过期时间
                    redisSessionService.refreshSession(sessionId);
                    
                    log.debug("用户 {} 会话验证成功", user.getName());
                } else {
                    log.debug("会话 {} 已过期或不存在", sessionId);
                }
            }
        } catch (Exception e) {
            log.error("会话验证过程中发生错误", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中获取会话ID
     */
    private String getSessionIdFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
