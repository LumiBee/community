package com.lumibee.hive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 调试控制器
 * 提供调试页面的访问
 */
@Controller
public class DebugController {

    /**
     * 提供 debug-auth.html 页面
     */
    @GetMapping("/debug-auth.html")
    @ResponseBody
    public ResponseEntity<String> getDebugAuthPage() {
        try {
            Resource resource = new ClassPathResource("static/debug-auth.html");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Error loading debug page: " + e.getMessage());
        }
    }

    /**
     * 提供 check-token.html 页面
     */
    @GetMapping("/check-token.html")
    @ResponseBody
    public ResponseEntity<String> getCheckTokenPage() {
        try {
            Resource resource = new ClassPathResource("static/check-token.html");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Error loading check token page: " + e.getMessage());
        }
    }

    /**
     * 调试 API 端点，返回请求头信息
     */
    @GetMapping("/api/debug/headers")
    @ResponseBody
    public ResponseEntity<String> getHeaders(jakarta.servlet.http.HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        headers.append("Request Headers:\n");
        
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue).append("\n");
        }
        
        headers.append("\nRequest Method: ").append(request.getMethod()).append("\n");
        headers.append("Request URI: ").append(request.getRequestURI()).append("\n");
        headers.append("Remote Address: ").append(request.getRemoteAddr()).append("\n");
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(headers.toString());
    }
}