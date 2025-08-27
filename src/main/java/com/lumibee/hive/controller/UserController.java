package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserService userService;

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

    @GetMapping("/debug/test-json-serialization")
    public ResponseEntity<Map<String, Object>> testJsonSerialization() {
        Map<String, Object> response = new HashMap<>();
        
        // 创建一个包含大整数ID的测试对象
        User testUser = new User();
        testUser.setId(1925216231290916865L);
        testUser.setName("TestUser");
        testUser.setEmail("test@example.com");
        
        response.put("testUser", testUser);
        response.put("testUserId", testUser.getId());
        response.put("testUserIdType", testUser.getId().getClass().getName());
        response.put("testUserIdAsString", testUser.getId().toString());
        
        // 测试Jackson序列化
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(testUser);
            response.put("jsonSerialization", json);
            
            // 测试反序列化
            User deserializedUser = objectMapper.readValue(json, User.class);
            response.put("deserializedUserId", deserializedUser.getId());
            response.put("deserializedUserIdType", deserializedUser.getId().getClass().getName());
            response.put("serializationWorks", testUser.getId().equals(deserializedUser.getId()));
            
        } catch (Exception e) {
            response.put("serializationError", e.getMessage());
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

    @GetMapping("/debug/test-following-status")
    public ResponseEntity<Map<String, Object>> testFollowingStatus(@AuthenticationPrincipal Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            response.put("success", false);
            response.put("error", "用户未登录");
            return ResponseEntity.ok(response);
        }
        
        // 测试关注状态检查
        Long currentUserId = currentUser.getId();
        Long authorId = 1925216231290916865L; // 作者ID
        
        response.put("currentUserId", currentUserId);
        response.put("authorId", authorId);
        response.put("currentUserIdType", currentUserId.getClass().getName());
        response.put("authorIdType", authorId.getClass().getName());
        
        // 检查关注状态
        boolean isFollowing = userService.isFollowing(currentUserId, authorId);
        response.put("isFollowing", isFollowing);
        
        // 检查数据库中的关注关系
        try {
            Integer dbResult = userService.getUserFollowingMapper().isFollowing(authorId, currentUserId);
            response.put("dbQueryResult", dbResult);
            response.put("dbQueryParams", "user_id=" + authorId + ", follower_id=" + currentUserId);
        } catch (Exception e) {
            response.put("dbQueryError", e.getMessage());
        }
        
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
