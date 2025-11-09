package com.lumibee.hive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置类
 * 优化静态资源的缓存策略和访问性能
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件资源 - 长期缓存
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(31536000) // 1年
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver());

        // 静态资源 - 长期缓存
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31536000) // 1年
                .resourceChain(true);

        // 公共资源 - 长期缓存
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/")
                .setCachePeriod(31536000) // 1年
                .resourceChain(true);

        // 图片资源 - 长期缓存
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/", "file:./uploads/")
                .setCachePeriod(31536000) // 1年
                .resourceChain(true);

        // CSS资源 - 长期缓存
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(31536000) // 1年
                .resourceChain(true);

        // JS资源 - 长期缓存
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(31536000) // 1年
                .resourceChain(true);

        // 字体资源 - 长期缓存
        registry.addResourceHandler("/fonts/**", "/woff2/**")
                .addResourceLocations("classpath:/static/fonts/", "classpath:/static/woff2/")
                .setCachePeriod(31536000) // 1年
                .resourceChain(true);

        // Favicon - 长期缓存
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31536000) // 1年
                .resourceChain(true);
    }
}
