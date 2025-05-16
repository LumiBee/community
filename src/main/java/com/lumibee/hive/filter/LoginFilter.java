package com.lumibee.hive.filter;

import com.lumibee.hive.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

 @Component
public class LoginFilter implements Filter {

    // 定义需要登录才能访问的路径列表
    final private List<String> protectedPaths = List.of("/publish");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String path = httpRequest.getServletPath();

        boolean isProtectedPath = protectedPaths.stream().anyMatch(path::startsWith);

        // 检查是否是受保护的路径并且用户未登录
        if (isProtectedPath) {
            User user = null;
            if (session != null) {
                user = (User) session.getAttribute("user");
            }
            if (user == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/"); // 直接重定向到首页
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}