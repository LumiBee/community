package com.lumibee.hive.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${file.base.url:/uploads/}")
    private String baseUrl;
    
    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将上传目录映射为可访问的静态资源
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        // 移除baseUrl开头和结尾的斜杠，以确保正确的路径格式
        String pathPattern = baseUrl;
        if (pathPattern.startsWith("/")) {
            pathPattern = pathPattern.substring(1);
        }
        if (!pathPattern.endsWith("/")) {
            pathPattern = pathPattern + "/";
        }
        pathPattern = pathPattern + "**";
        
        registry.addResourceHandler(pathPattern)
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }
} 