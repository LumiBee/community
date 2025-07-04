package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;

@Controller
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserService userService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Map<String, Object>> toggleFollow(@PathVariable("userId") Long userId,
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
}
