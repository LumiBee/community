package com.lumibee.hive.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import com.lumibee.hive.service.CacheService;
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
import com.lumibee.hive.service.ImgService;
import com.lumibee.hive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户，关注，更新资料相关的 API 接口")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private ImgService imgService;
    @Autowired private CacheService cacheService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @ResponseBody
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Principal principal) {

        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/follow")
    @Operation(summary = "切换关注状态", description = "关注或取消关注指定用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Map<String, Object>> toggleFollow(
            @Parameter(description = "要关注的用户ID") @PathVariable("userId") Long userId,
            @AuthenticationPrincipal Principal principal) {


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
        
        // 验证用户是否存在
        User targetUser = userService.selectById(userId);
        if (targetUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "要关注的用户不存在");
            return ResponseEntity.badRequest().body(response);
        }
        
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


    /**
     * 检查当前用户是否关注了指定用户
     */
    @GetMapping("/{userId}/is-following")
    @Operation(summary = "检查用户是否关注", description = "检查当前用户是否关注了指定用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "检查成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Map<String, Object>> isFollowing(
            @Parameter(description = "要检查的用户ID") @PathVariable("userId") Long userId,
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
    @Operation(summary = "上传头像", description = "上传用户头像")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("avatar") MultipartFile avatarFile,
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
    @Operation(summary = "更新用户资料", description = "更新当前用户的用户名、邮箱或个人简介")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "资料更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Parameter(description = "新的用户名") @RequestParam("userName") String userName,
            @Parameter(description = "新的邮箱") @RequestParam("email") String email,
            @Parameter(description = "新的个人简介", required = false) @RequestParam(value = "bio", required = false) String bio,
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
                    cacheService.clearUserRelatedCaches(currentUser.getId(), currentUser.getName());
                } else {
                    // 如果只是其他信息变化，只清理用户相关缓存
                    cacheService.clearUserArticleCaches(currentUser.getId());
                }
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
