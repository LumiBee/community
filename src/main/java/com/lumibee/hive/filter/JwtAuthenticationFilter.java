package com.lumibee.hive.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;
import com.lumibee.hive.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT认证过滤器
 * 用于从请求头中提取JWT令牌并验证
 */
@Component
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("=== JwtAuthenticationFilter 处理请求 ===");
        log.info("请求URI: {}", request.getRequestURI());
        log.info("请求方法: {}", request.getMethod());
        log.info("请求URL: {}", request.getRequestURL());

        try {
            // 从请求中获取JWT
            String jwt = getJwtFromRequest(request);
            log.info("提取的JWT: {}", jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : "null");

            // 检查JWT是否存在
            if (StringUtils.hasText(jwt)) {
                log.info("JWT存在，开始验证...");
                try {
                    // 验证令牌
                    boolean isValid = jwtUtil.validateToken(jwt);
                    log.info("JWT验证结果: {}", isValid);

                    // 如果令牌有效，则设置认证信息
                    if (isValid) {
                        // 从JWT中获取用户ID
                        Long userId = jwtUtil.getUserIdFromToken(jwt);

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
                            log.info("用户认证成功，用户ID: {}, 用户名: {}", userId, user.getName());
                        } else {
                            // 即使JWT有效，但找不到用户
                            log.warn("JWT was valid, but no user found with ID: {}", userId);
                            SecurityContextHolder.clearContext();
                        }
                    }
                } catch (Exception e) {
                    // 记录日志，清空上下文，然后“吞掉”异常，让请求继续（作为匿名用户）
                    log.warn("JWT validation/processing failed for request {}. Error: {}", request.getRequestURI(), e.getMessage());
                    // 清空上下文，让请求作为匿名用户继续
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.info("请求中没有JWT，作为匿名用户继续");
            }
        } catch (Exception ex) {
            log.error("Error in JwtAuthenticationFilter (outside JWT processing)", ex);
            SecurityContextHolder.clearContext();
        }

        log.info("JwtAuthenticationFilter处理完成，继续过滤器链");
        // 无论如何，都继续过滤器链
        filterChain.doFilter(request, response);
        log.info("=== JwtAuthenticationFilter 请求处理结束 ===");
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
    
}
