package com.lumibee.hive.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.dto.SignupDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "注册管理", description = "注册相关的 API 接口")
public class SignupController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 处理注册页面请求
     * 不再使用重定向，避免CORS预检请求被重定向
     */
    @GetMapping("/signup")
    @Operation(summary = "获取注册页面", description = "处理注册页面请求")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "页面获取成功")
    })
    public ResponseEntity<String> handleSignupPage() {
        // 返回一个简单的响应，前端路由会处理实际的页面渲染
        return ResponseEntity.ok().body("{}");
    }

    /**
     * 处理用户注册API
     */
    @PostMapping("/signup")
    @Operation(summary = "用户注册", description = "处理新用户注册请求")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "注册成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误或用户已存在")
    })
    public ResponseEntity<Map<String, Object>> processSignup(
            @Parameter(description = "注册信息") @Valid @RequestBody SignupDTO signupDTO,
            @Parameter(description = "验证结果") BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        // 1. 基本表单校验结果 (来自 SignupDTO 的注解)
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
        }

        // 2. 检查密码和确认密码是否一致
        if (!signupDTO.getPassword().equals(signupDTO.getConfirmPassword())) {
            errors.put("confirmPassword", "密码和确认密码不一致");
        }

        // 3. 检查用户名是否已存在
        User existingUserByName = userService.selectByName(signupDTO.getUsername());
        if (existingUserByName != null) {
            errors.put("username", "该用户名已被注册");
        }

        // 4. 检查邮箱是否已存在
        User existingUserByEmail = userService.selectByEmail(signupDTO.getEmail());
        if (existingUserByEmail != null) {
            errors.put("email", "该邮箱已被注册");
        }

        // 如果存在任何校验错误
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        // 5. 如果校验通过，创建新用户
        User newUser = new User();
        newUser.setName(signupDTO.getUsername());
        newUser.setEmail(signupDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(signupDTO.getPassword())); // 加密密码
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setGmtCreate(LocalDateTime.now());
        newUser.setGmtModified(newUser.getGmtCreate());
        newUser.setAvatarUrl(null);

        try {
            userService.insert(newUser);
        } catch (Exception e) {
            e.printStackTrace(); // 记录错误
            response.put("success", false);
            response.put("message", "注册失败，请稍后再试或联系管理员。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // 6. 注册成功
        response.put("success", true);
        response.put("message", "注册成功！现在您可以使用新账户登录了。");
        response.put("redirectUrl", "/login");
        
        return ResponseEntity.ok(response);
    }
}