package com.lumibee.hive.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.CacheService;
import com.lumibee.hive.service.ImgService;
import com.lumibee.hive.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private ImgService imgService;
    @Autowired private CacheService cacheService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @ResponseBody
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Principal principal) {

        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            System.out.println("用户未找到，返回401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        System.out.println("找到用户: " + user.getName());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Map<String, Object>> toggleFollow(@PathVariable("userId") Long userId,
                                                            @AuthenticationPrincipal Principal principal) {

        System.out.println("=== toggleFollow 被调用 ===");
        System.out.println("接收到的用户ID (Long): " + userId);
        System.out.println("用户ID类型: " + (userId != null ? userId.getClass().getName() : "null"));

        // 获取当前用户
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 检查是否尝试关注自己
        if (currentUser.getId().equals(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "不能关注自己哦，试试关注其他作者吧！");
            return ResponseEntity.badRequest().body(response);
        }

        // 调用服务层方法切换关注状态
        // 注意：这里是当前用户(currentUser)关注作者(userId)
        // 所以参数顺序应该是 currentUser.getId(), userId
        System.out.println("关注操作 - 当前用户ID: " + currentUser.getId() + ", 要关注的用户ID: " + userId);
        
        // 验证用户是否存在
        User targetUser = userService.selectById(userId);
        if (targetUser == null) {
            System.out.println("错误：要关注的用户不存在，ID: " + userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "要关注的用户不存在");
            return ResponseEntity.badRequest().body(response);
        }
        System.out.println("要关注的用户存在: " + targetUser.getName());
        
        boolean isFollowing = userService.toggleFollow(currentUser.getId(), userId);
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isFollowing", isFollowing);
        
        // 添加提示消息
        if (isFollowing) {
            response.put("message", "关注成功！将为您推送作者的最新内容");
        } else {
            response.put("message", "已取消关注");
        }

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/debug/users")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取所有用户的基本信息
            List<User> users = userService.getUserMapper().selectList(null);
            List<Map<String, Object>> userList = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("name", user.getName());
                userInfo.put("email", user.getEmail());
                userList.add(userInfo);
            }
            
            response.put("success", true);
            response.put("users", userList);
            response.put("count", userList.size());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug/test-id/{userId}")
    public ResponseEntity<Map<String, Object>> testUserId(@PathVariable("userId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("success", true);
        response.put("receivedUserId", userId);
        response.put("userIdType", userId != null ? userId.getClass().getName() : "null");
        response.put("userIdAsString", String.valueOf(userId));
        
        // 尝试查找用户
        User user = userService.selectById(userId);
        if (user != null) {
            response.put("userFound", true);
            response.put("userName", user.getName());
            response.put("userEmail", user.getEmail());
        } else {
            response.put("userFound", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查当前用户是否关注了指定用户
     */
    @GetMapping("/{userId}/is-following")
    public ResponseEntity<Map<String, Object>> isFollowing(@PathVariable("userId") Long userId,
                                                          @AuthenticationPrincipal Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        // 获取当前用户
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            response.put("success", false);
            response.put("error", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        // 检查关注状态
        boolean isFollowing = userService.isFollowing(currentUser.getId(), userId);
        
        response.put("success", true);
        response.put("isFollowing", isFollowing);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("avatar") MultipartFile avatarFile,
                                                           @AuthenticationPrincipal Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUserFromPrincipal(principal);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 验证文件
            if (avatarFile == null || avatarFile.isEmpty()) {
                response.put("success", false);
                response.put("message", "请选择头像文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证文件类型
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "请选择图片文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证文件大小 (2MB)
            if (avatarFile.getSize() > 2 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "文件大小不能超过2MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 上传头像
            String avatarUrl = imgService.uploadAvatar(currentUser.getId(), avatarFile);
            
            // 更新用户头像URL
            currentUser.setAvatarUrl(avatarUrl);
            userService.updateById(currentUser);
            
            // 自动清理相关缓存
            try {
                cacheService.clearUserArticleCaches(currentUser.getId());
                System.out.println("用户头像更新后，已自动清理相关缓存");
            } catch (Exception e) {
                System.err.println("清理缓存失败: " + e.getMessage());
                // 缓存清理失败不影响头像上传的成功
            }
            
            // 返回成功响应
            response.put("success", true);
            response.put("message", "头像上传成功");
            response.put("avatarUrl", avatarUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            System.err.println("头像上传失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "头像上传失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("头像上传异常: " + e.getMessage());
            response.put("success", false);
            response.put("message", "头像上传失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam("userName") String userName,
            @RequestParam("email") String email,
            @RequestParam(value = "bio", required = false) String bio,
            @AuthenticationPrincipal Principal principal) {
        
        Map<String, Object> response = new HashMap<>();

        // 获取当前用户
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 前端验证
        if (userName == null || userName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "用户名不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        if (email == null || email.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "邮箱不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证邮箱格式
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            response.put("success", false);
            response.put("message", "请输入有效的邮箱地址");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证用户名长度
        if (userName.length() < 2 || userName.length() > 20) {
            response.put("success", false);
            response.put("message", "用户名长度必须在2-20个字符之间");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证个人简介长度
        if (bio != null && bio.length() > 500) {
            response.put("success", false);
            response.put("message", "个人简介不能超过500个字符");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 检查用户名是否发生变化
            boolean userNameChanged = !userName.trim().equals(currentUser.getName());
            
            User updatedUser = userService.updateProfile(
                currentUser.getId(), 
                userName.trim(), 
                email.trim(), 
                bio != null ? bio.trim() : null
            );
            
            // 自动清理相关缓存
            try {
                if (userNameChanged) {
                    // 如果用户名发生变化，清理用户名相关的所有缓存
                    System.out.println("检测到用户名变化，清理用户名相关缓存");
                    cacheService.clearUserNameChangeCaches();
                } else {
                    // 如果只是其他信息变化，只清理用户相关缓存
                    cacheService.clearUserArticleCaches(currentUser.getId());
                }
                System.out.println("用户资料更新后，已自动清理相关缓存");
            } catch (Exception e) {
                System.err.println("清理缓存失败: " + e.getMessage());
                // 缓存清理失败不影响资料更新的成功
            }
            
            response.put("success", true);
            response.put("message", "资料更新成功");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("更新用户资料失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "更新用户资料失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
