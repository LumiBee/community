package com.lumibee.hive.controller;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.security.Principal;

@RestController
@RequestMapping("/api/article")
@Tag(name = "文章发布", description = "文章发布、编辑、删除相关的 API 接口")
public class PublishController {

    @Autowired private UserService userService;
    @Autowired private ArticleService articleService;

    @PostMapping("/publish")
    @Operation(summary = "发布文章", description = "发布新文章到平台")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "文章发布成功", 
                    content = @Content(schema = @Schema(implementation = ArticleDetailsDTO.class))),
        @ApiResponse(responseCode = "401", description = "用户未认证"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<ArticleDetailsDTO> publishArticle(
        @Parameter(description = "当前用户") @AuthenticationPrincipal Principal principal,
        @Parameter(description = "文章发布请求数据") @RequestBody ArticlePublishRequestDTO requestDTO) {

        System.out.println("PublishController.publishArticle 被调用");
        System.out.println("Principal: " + (principal != null ? principal.getClass().getName() : "null"));
        
        // 检查用户是否已认证
        if (principal == null) {
            System.out.println("用户未认证: principal 为 null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        // 获取当前用户
        var user = userService.getCurrentUserFromPrincipal(principal);
        System.out.println("获取到用户: " + (user != null ? "ID=" + user.getId() + ", 名称=" + user.getName() : "null"));
        
        if (user == null) {
            System.out.println("用户未认证: 无法从 principal 获取用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        Long userId = user.getId();
        System.out.println("发布文章请求: " + requestDTO);
        ArticleDetailsDTO newArticle = articleService.publishArticle(requestDTO, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newArticle);
    }

    @PutMapping("/{articleId}/edit")
    @Operation(summary = "编辑文章", description = "编辑已发布的文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文章编辑成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证"),
        @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<ArticleDetailsDTO> editArticle(
        @Parameter(description = "文章ID") @PathVariable Integer articleId,
        @Parameter(description = "当前用户") @AuthenticationPrincipal Principal principal,
        @Parameter(description = "文章编辑请求数据") @RequestBody ArticlePublishRequestDTO requestDTO) {
        
        // 检查用户是否已认证
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        // 获取当前用户
        var user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        Long userId = user.getId();
        ArticleDetailsDTO updatedArticle = articleService.updateArticle(articleId, requestDTO, userId);

        return ResponseEntity.ok(updatedArticle);
    }

    @DeleteMapping("/delete/{articleId}")
    @Operation(summary = "删除文章", description = "删除已发布的文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文章删除成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证"),
        @ApiResponse(responseCode = "404", description = "文章不存在")
    })
    public ResponseEntity<ArticleDetailsDTO> deleteArticle(
        @Parameter(description = "文章ID") @PathVariable Integer articleId,
        @Parameter(description = "当前用户") @AuthenticationPrincipal Principal principal) {
        
        // 检查用户是否已认证
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        // 获取当前用户
        var user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        Long userId = user.getId();
        ArticleDetailsDTO deletedArticle = articleService.deleteArticleById(articleId, userId);

        if (deletedArticle != null) {
            return ResponseEntity.ok(deletedArticle);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


}
