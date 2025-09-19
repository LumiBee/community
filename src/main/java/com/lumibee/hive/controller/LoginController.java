package com.lumibee.hive.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.RedisRememberMeService;
import com.lumibee.hive.service.RedisSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@Tag(name = "登录管理", description = "登录相关的 API 接口")
public class LoginController {
    
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private RedisRememberMeService rememberMeService;
    
    @Autowired
    private RedisSessionService redisSessionService;
    
    @Value("${app.session.timeout:1800}") // 30分钟
    private int sessionTimeoutSeconds;

    /**
     * 重定向登录页面到Vue SPA
     */
    @GetMapping("/login")
    @Operation(summary = "重定向到登录页面", description = "重定向到Vue SPA的登录页面")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "重定向到前端登录页面")
    })
    public void redirectToLoginSPA(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 重定向到前端登录页面
        String frontendUrl = "https://www.hivelumi.com/login";
        response.sendRedirect(frontendUrl);
    }

    /**
     * API登录端点，用于处理前端AJAX登录请求
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "处理用户登录请求，支持记住我功能")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "登录失败，用户名或密码错误"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<?> apiLogin(
            @Parameter(description = "登录请求参数") @RequestBody Map<String, String> loginRequest, 
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        // 输入验证
        String account = loginRequest.get("account");
        String password = loginRequest.get("password");
        String rememberMe = loginRequest.get("remember-me");
        
        if (account == null || account.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户名不能为空"));
        }
        
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("密码不能为空"));
        }
        
        try {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(account.trim(), password);
            
            // 尝试认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // 认证成功，设置安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = (User) authentication.getPrincipal();
            
            // 生成会话ID
            String sessionId = UUID.randomUUID().toString();
            
            // 存储到Redis Session
            redisSessionService.storeSession(sessionId, user);
            
            // 设置会话Cookie
            Cookie sessionCookie = new Cookie("session", sessionId);
            sessionCookie.setHttpOnly(true);
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(sessionTimeoutSeconds);
            response.addCookie(sessionCookie);
            
            // 处理remember-me功能
            if ("on".equals(rememberMe)) {
                String rememberMeToken = rememberMeService.createRememberMeToken(user);
                rememberMeService.setRememberMeCookie(response, rememberMeToken);
            }
            
            // 返回成功响应
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("user", user);
            responseMap.put("message", "登录成功");
            responseMap.put("sessionId", sessionId);
            
            return ResponseEntity.ok(responseMap);
            
        } catch (AuthenticationException e) {
            log.warn("登录失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("用户名或密码错误"));
        } catch (Exception e) {
            log.error("登录过程中发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("登录过程中发生错误，请稍后重试"));
        }
    }


    /**
     * 关闭密码设置提示
     */
    @PostMapping("/user/dismiss-password-prompt")
    public ResponseEntity<Map<String, String>> dismissPasswordPrompt(HttpServletRequest request) {
        // 从Redis Session中获取用户信息
        String sessionId = getSessionIdFromRequest(request);
        if (sessionId != null) {
            User user = redisSessionService.getSession(sessionId);
            if (user != null) {
                // 可以在这里处理密码提示的关闭逻辑
                // 比如在用户表中添加标记字段
                log.info("用户 {} 关闭了密码设置提示", user.getName());
            }
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Prompt dismissed");
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * API登出端点，用于处理前端AJAX登出请求
     */
    @PostMapping("/logout")
    @Operation(summary = "登出状态", description = "切换为登出状态")
    public ResponseEntity<Map<String, Object>> apiLogout(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // 获取会话ID
            String sessionId = getSessionIdFromRequest(request);
            
            // 清除Redis Session
            if (sessionId != null) {
                redisSessionService.deleteSession(sessionId);
                log.info("用户会话 {} 已清除", sessionId);
            }
            
            // 清除安全上下文
            SecurityContextHolder.clearContext();
            
            // 清除会话Cookie
            Cookie sessionCookie = new Cookie("session", "");
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(0);
            response.addCookie(sessionCookie);
            
            // 清除remember-me cookie
            Cookie rememberMeCookie = new Cookie("remember-me", "");
            rememberMeCookie.setPath("/");
            rememberMeCookie.setMaxAge(0);
            response.addCookie(rememberMeCookie);
            
            responseMap.put("success", true);
            responseMap.put("message", "登出成功");
            
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            log.error("登出过程中发生错误", e);
            responseMap.put("success", false);
            responseMap.put("message", "登出过程中发生错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
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
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}