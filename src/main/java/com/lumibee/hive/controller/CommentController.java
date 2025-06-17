package com.lumibee.hive.controller;

import com.lumibee.hive.dto.CommentDTO;
import com.lumibee.hive.dto.UserDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.CommentService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/article/{articleId}")
public class CommentController {
    @Autowired private CommentService commentService;
    @Autowired private UserService userService;

    @GetMapping("/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable("articleId") Integer articleId) {
        List<CommentDTO> comments = commentService.getCommentsByArticleId(articleId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/comment")
    public ResponseEntity<?> postComment(@PathVariable("articleId") Integer articleId,
                                         @RequestBody Map<String, String> payload,
                                         @AuthenticationPrincipal Principal principal) {
        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "请先登录"));
        }
        Long userId = user.getId();

        String content = payload.get("content");
        String parentIdStr = payload.get("parentId");
        Long parentId = (parentIdStr != null && !parentIdStr.isEmpty()) ? Long.parseLong(parentIdStr) : null;

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "评论内容不能为空"));
        }

        commentService.addComment(articleId, content, userId, parentId);
        return ResponseEntity.ok(Map.of("success", true, "message", "评论成功"));
    }
}
