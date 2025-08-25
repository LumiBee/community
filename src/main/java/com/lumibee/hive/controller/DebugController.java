package com.lumibee.hive.controller;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 调试用API - 获取用户信息
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userService.selectByName(username);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }
        
        response.put("success", true);
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("passwordExists", user.getPassword() != null);
        response.put("passwordLength", user.getPassword() != null ? user.getPassword().length() : 0);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 调试用API - 重置用户密码
     */
    @GetMapping("/reset-password/{username}/{password}")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable String username, 
            @PathVariable String password) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userService.selectByName(username);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }
        
        // 使用Spring Security的密码编码器
        String encodedPassword = passwordEncoder.encode(password);
        
        // 更新用户密码
        boolean updated = userService.updatePassword(user.getId(), encodedPassword);
        
        response.put("success", updated);
        response.put("message", updated ? "密码已更新" : "密码更新失败");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 调试用API - 清除用户缓存
     */
    @GetMapping("/clear-cache/{username}")
    public ResponseEntity<Map<String, Object>> clearCache(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userService.selectByName(username);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }
        
        // 强制更新用户信息，清除缓存
        user.setGmtModified(java.time.LocalDateTime.now());
        userService.updateById(user);
        
        response.put("success", true);
        response.put("message", "用户缓存已清除");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 调试用API - 直接从数据库获取用户信息（绕过缓存）
     */
    @GetMapping("/db-user/{username}")
    public ResponseEntity<Map<String, Object>> getDbUserInfo(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        // 直接使用Mapper查询，绕过缓存
        User user = userService.getUserMapper().selectByName(username);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }
        
        response.put("success", true);
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("passwordExists", user.getPassword() != null);
        response.put("passwordLength", user.getPassword() != null ? user.getPassword().length() : 0);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 调试用API - 测试密码验证
     */
    @GetMapping("/verify-password/{username}/{password}")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @PathVariable String username, 
            @PathVariable String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(username, password);
            
            // 尝试认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // 认证成功
            response.put("success", true);
            response.put("message", "密码验证成功");
            response.put("authenticated", authentication.isAuthenticated());
            
        } catch (AuthenticationException e) {
            response.put("success", false);
            response.put("message", "密码验证失败: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "验证过程中发生错误: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 调试用API - 检查密码匹配
     */
    @GetMapping("/check-password/{username}/{password}")
    public ResponseEntity<Map<String, Object>> checkPassword(
            @PathVariable String username, 
            @PathVariable String password) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userService.getUserMapper().selectByName(username);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.ok(response);
        }
        
        if (user.getPassword() == null) {
            response.put("success", false);
            response.put("message", "用户密码未设置");
            return ResponseEntity.ok(response);
        }
        
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        
        response.put("success", true);
        response.put("passwordMatches", matches);
        response.put("message", matches ? "密码匹配" : "密码不匹配");
        response.put("storedPasswordHash", user.getPassword());
        
        return ResponseEntity.ok(response);
    }
}