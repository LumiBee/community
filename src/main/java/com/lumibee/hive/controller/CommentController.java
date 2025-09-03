package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.dto.CommentDTO;
import com.lumibee.hive.model.Comments;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.CommentService;
import com.lumibee.hive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/comments")
@Tag(name = "评论管理", description = "文章评论相关的 API 接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @GetMapping("/article/{articleId}")
    @Operation(summary = "获取文章评论", description = "根据文章ID获取该文章的所有评论")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<CommentDTO>> getCommentsByArticleId(
            @Parameter(description = "文章ID") @PathVariable Integer articleId) {
        List<CommentDTO> comments = commentService.getCommentsByArticleId(articleId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/add")
    @Operation(summary = "添加评论", description = "为指定文章添加新评论")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "评论添加成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Comments> addComment(
            @Parameter(description = "文章ID") @RequestParam Integer articleId,
            @Parameter(description = "评论内容") @RequestParam String content,
            @Parameter(description = "父评论ID", required = false) @RequestParam(required = false) Long parentId,
            @AuthenticationPrincipal Principal principal) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Comments newComment = commentService.addComment(articleId, content, currentUser.getId(), parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }
}
