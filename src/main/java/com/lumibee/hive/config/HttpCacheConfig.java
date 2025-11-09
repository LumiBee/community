package com.lumibee.hive.config;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP缓存配置类
 * 为不同类型的资源设置合适的缓存策略
 */
@Configuration
public class HttpCacheConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CacheControlInterceptor())
                .addPathPatterns("/uploads/**", "/static/**", "/public/**", "/img/**", "/css/**", "/js/**", "/fonts/**", "/woff2/**", "/favicon.ico");
    }

    /**
     * 缓存控制拦截器
     */
    public static class CacheControlInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String requestURI = request.getRequestURI();
            
            // 设置缓存控制头
            if (isStaticResource(requestURI)) {
                // 静态资源 - 长期缓存
                response.setHeader(HttpHeaders.CACHE_CONTROL, 
                    CacheControl.maxAge(Duration.ofDays(365))
                        .cachePublic()
                        .immutable()
                        .getHeaderValue());
                
                // 设置ETag支持
                response.setHeader(HttpHeaders.ETAG, "\"" + System.currentTimeMillis() + "\"");
                
                // 设置过期时间
                response.setDateHeader(HttpHeaders.EXPIRES, 
                    System.currentTimeMillis() + Duration.ofDays(365).toMillis());
                
                // 设置Vary头
                response.setHeader(HttpHeaders.VARY, "Accept-Encoding");
                
            } else if (isApiEndpoint(requestURI)) {
                // API端点 - 短期缓存或禁用缓存
                response.setHeader(HttpHeaders.CACHE_CONTROL, 
                    CacheControl.noCache()
                        .mustRevalidate()
                        .getHeaderValue());
            }
            
            return true;
        }

        /**
         * 判断是否为静态资源
         */
        private boolean isStaticResource(String uri) {
            return uri.startsWith("/uploads/") ||
                   uri.startsWith("/static/") ||
                   uri.startsWith("/public/") ||
                   uri.startsWith("/img/") ||
                   uri.startsWith("/css/") ||
                   uri.startsWith("/js/") ||
                   uri.startsWith("/fonts/") ||
                   uri.startsWith("/woff2/") ||
                   uri.equals("/favicon.ico");
        }

        /**
         * 判断是否为API端点
         */
        private boolean isApiEndpoint(String uri) {
            return uri.startsWith("/api/");
        }
    }
}
