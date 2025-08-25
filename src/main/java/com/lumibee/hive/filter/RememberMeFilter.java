package com.lumibee.hive.filter;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.RememberMeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RememberMeFilter extends OncePerRequestFilter {
    
    @Autowired
    private RememberMeService rememberMeService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 如果用户已经认证，跳过此过滤器
        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 检查是否有remember-me cookie
        String rememberMeToken = rememberMeService.getRememberMeTokenFromRequest(request);
        if (rememberMeToken != null) {
            try {
                // 验证token
                User user = rememberMeService.validateRememberMeToken(rememberMeToken);
                if (user != null) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    
                    // 设置认证上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 设置session
                    HttpSession session = request.getSession(true);
                    session.setAttribute("user", user);
                    session.setAttribute("rememberMe", true);
                    
                    System.out.println("Remember-me自动登录成功: " + user.getName());
                } else {
                    // token无效，清除cookie
                    rememberMeService.clearRememberMeCookie(response);
                    rememberMeService.removeRememberMeToken(rememberMeToken);
                }
            } catch (Exception e) {
                // 处理异常，清除无效token
                rememberMeService.clearRememberMeCookie(response);
                rememberMeService.removeRememberMeToken(rememberMeToken);
                System.err.println("Remember-me token验证失败: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
