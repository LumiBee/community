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
    // 注意：路径匹配是基于 HttpServletRequest.getServletPath() 或 getRequestURI()
    // 如果您的 publish.html 是通过 /publish 这样的控制器路径访问的，请使用 /publish
    final private List<String> protectedPaths = List.of("/publish");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter 初始化逻辑 (如果需要)
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false); // false表示如果session不存在则不创建新的

        String path = httpRequest.getServletPath(); // 获取请求的 servlet 路径，例如 /publish

        boolean isProtectedPath = protectedPaths.stream().anyMatch(path::startsWith);

        // 检查是否是受保护的路径并且用户未登录
        if (isProtectedPath) {
            User user = null;
            if (session != null) {
                user = (User) session.getAttribute("user"); // "user" 是您在session中存储用户对象的键名
            }
            if (user == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/"); // 直接重定向到首页
                return; // 中断请求链
            }
        }

        // 用户已登录或访问的不是受保护路径，继续执行请求链
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Filter 销毁逻辑 (如果需要)
        Filter.super.destroy();
    }
}