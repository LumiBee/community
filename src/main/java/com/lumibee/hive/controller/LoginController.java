package com.lumibee.hive.controller;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;
import com.lumibee.hive.service.RememberMeService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {
    
    // JWT密钥 - 实际应用中应该放在配置文件中
    private static final String JWT_SECRET = "lumiHiveSecretKeyForJwtAuthenticationToken12345";
    // JWT过期时间 - 24小时
    private static final long JWT_EXPIRATION = 86400000;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private RememberMeService rememberMeService;
    
    /**
     * 生成JWT令牌
     * @param user 用户对象
     * @return JWT令牌字符串
     */
    private String generateJwtToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);
        
        // 创建JWT密钥
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        
        // 创建JWT令牌
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 重定向登录页面到Vue SPA
     */
    @GetMapping("/login")
    public ResponseEntity<Void> redirectToLoginSPA() {
        // 直接返回成功状态，让前端Vue路由处理
        return ResponseEntity.ok().build();
    }

    /**
     * API登录端点，用于处理前端AJAX登录请求
     */
    @PostMapping("/api/login")
    public ResponseEntity<?> apiLogin(@RequestBody Map<String, String> loginRequest, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        String account = loginRequest.get("account");
        String password = loginRequest.get("password");
        String rememberMe = loginRequest.get("remember-me");
        
        System.out.println("接收到登录请求: account=" + account + ", password长度=" + (password != null ? password.length() : 0) + ", rememberMe=" + rememberMe);
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(account, password);
            
            System.out.println("准备认证...");
            
            System.out.println("准备开始认证过程...");
            // 尝试认证
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            System.out.println("认证成功，设置安全上下文");
            System.out.println("认证结果: " + authentication);
            System.out.println("认证主体类型: " + authentication.getPrincipal().getClass().getName());
            
            // 认证成功，设置安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
                            // 获取用户信息
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                System.out.println("获取到UserDetails: " + userDetails.getClass().getName());
                System.out.println("UserDetails.getUsername(): " + userDetails.getUsername());
                System.out.println("UserDetails.getPassword(): " + (userDetails.getPassword() != null ? "已设置" : "未设置"));
                
                // 由于User实现了UserDetails，我们可以直接转换为User对象
                User user = (User) userDetails;
                System.out.println("转换为User对象: ID=" + user.getId() + ", Name=" + user.getName());
                
                // 生成JWT令牌并设置到用户对象中
                String jwtToken = generateJwtToken(user);
                user.setToken(jwtToken);
                System.out.println("已生成JWT令牌并设置到用户对象中");
            
            if (user != null) {
                // 处理remember-me功能
                if ("on".equals(rememberMe)) {
                    System.out.println("启用remember-me功能");
                    // 使用RememberMeService创建token
                    String rememberMeToken = rememberMeService.createRememberMeToken(user);
                    // 设置remember-me cookie
                    rememberMeService.setRememberMeCookie(response, rememberMeToken);
                    
                    // 在session中标记用户选择了remember-me
                    session.setAttribute("rememberMe", true);
                    System.out.println("Remember-me token已创建并设置cookie");
                }
                
                // 将用户信息存入会话
                session.setAttribute("user", user);
                
                // 返回用户信息
                responseMap.put("success", true);
                responseMap.put("user", user);
                responseMap.put("message", "登录成功");
                
                return ResponseEntity.ok(responseMap);
            } else {
                responseMap.put("success", false);
                responseMap.put("message", "登录成功但无法获取用户信息");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
            }
        } catch (AuthenticationException e) {
            responseMap.put("success", false);
            responseMap.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            
            responseMap.put("success", false);
            responseMap.put("message", "登录过程中发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }
    


    /**
     * 获取登录页面状态API
     */
    @GetMapping("/api/login-status")
    public ResponseEntity<Map<String, Object>> getLoginStatus(
            @RequestParam(value = "signupSuccess", required = false) String signupSuccess,
            @RequestParam(value = "signupMessage", required = false) String signupMessage) {
        
        Map<String, Object> response = new HashMap<>();
        
        if ("true".equals(signupSuccess) && signupMessage != null) {
            response.put("showSignupSuccessPopup", true);
            response.put("popupMessage", signupMessage);
        } else {
            response.put("showSignupSuccessPopup", false);
            response.put("popupMessage", null);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 关闭密码设置提示
     */
    @PostMapping("/api/user/dismiss-password-prompt")
    public ResponseEntity<Map<String, String>> dismissPasswordPrompt(HttpSession session) {
        if (session != null) {
            session.removeAttribute("showPasswordSetupPrompt");
            System.out.println("Session attribute 'showPasswordSetupPrompt' removed by user action.");
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Prompt dismissed");
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * API登出端点，用于处理前端AJAX登出请求
     */
    @PostMapping("/api/logout")
    public ResponseEntity<Map<String, Object>> apiLogout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            System.out.println("开始处理API登出请求");
            
            // 清除安全上下文
            SecurityContextHolder.clearContext();
            System.out.println("安全上下文已清除");
            
            // 清除会话
            if (session != null) {
                session.invalidate();
                System.out.println("会话已失效");
            }
            
            // 清除所有相关的cookie
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JSESSIONID".equals(cookie.getName()) || 
                        "remember-me".equals(cookie.getName()) ||
                        "XSRF-TOKEN".equals(cookie.getName())) {
                        cookie.setValue("");
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                        System.out.println("已清除cookie: " + cookie.getName());
                    }
                }
            }
            
            responseMap.put("success", true);
            responseMap.put("message", "登出成功");
            System.out.println("API登出处理完成");
            
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            System.err.println("登出过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            
            responseMap.put("success", false);
            responseMap.put("message", "登出过程中发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

}
