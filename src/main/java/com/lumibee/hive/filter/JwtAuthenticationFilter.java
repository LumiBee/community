package com.lumibee.hive.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT认证过滤器
 * 用于从请求头中提取JWT令牌并验证
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT密钥 - 实际应用中应该放在配置文件中
    private static final String JWT_SECRET = "lumiHiveSecretKeyForJwtAuthenticationToken12345";

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 记录请求信息
            
            // 检查是否是作品集创建请求
            if (request.getMethod().equals("POST") && request.getRequestURI().equals("/api/portfolio")) {
                java.util.Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                }
            }
            
            // 从请求头中获取JWT令牌
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                
                // 验证令牌
                boolean isValid = validateToken(jwt);
                
                // 如果令牌有效，则设置认证信息
                if (isValid) {
                    // 从JWT中获取用户ID
                    Long userId = getUserIdFromToken(jwt);
                    
                    // 根据用户ID获取用户信息
                    User user = userService.selectById(userId);
                    
                    if (user != null) {
                        
                        // 创建认证令牌
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        
                        // 设置认证详情
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                    } else {
                    }
                }
            } else {
            }
        } catch (Exception ex) {
            logger.error("JWT认证过程中发生错误", ex);
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求头中获取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 从请求头中获取JWT令牌
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 如果请求头中没有，尝试从请求参数中获取
        String paramToken = request.getParameter("token");
        if (StringUtils.hasText(paramToken)) {
            return paramToken;
        }
        
        // 如果请求参数中没有，尝试从cookie中获取
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // 记录所有请求头，用于调试
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
        }
        
        return null;
    }
    
    /**
     * 验证JWT令牌
     */
    private boolean validateToken(String token) {
        try {
            // 创建JWT密钥
            Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            
            // 解析JWT令牌
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
                
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT令牌验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从JWT令牌中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        // 创建JWT密钥
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        
        // 解析JWT令牌
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
            
        // 返回用户ID
        return Long.parseLong(claims.getSubject());
    }
}
