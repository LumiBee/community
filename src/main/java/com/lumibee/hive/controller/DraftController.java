package com.lumibee.hive.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "草稿管理", description = "文章草稿相关的 API 接口")
public class DraftController {
    @Autowired private ArticleService articleService;
    @Autowired private UserService userService;

    @PostMapping("/article/save-draft")
    @ResponseBody
    @Operation(summary = "保存草稿", description = "保存文章草稿或更新现有草稿")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "草稿保存成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<?> saveDraft(
            @Parameter(description = "草稿数据") @RequestBody ArticlePublishRequestDTO requestDTO,
            @AuthenticationPrincipal Principal principal) throws Exception {
        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        if (requestDTO.getArticleId() != null) {
            ArticleDetailsDTO updatedDraft = articleService.saveDraft(requestDTO.getArticleId(), requestDTO, user.getId());
            return ResponseEntity.ok(updatedDraft);
        }else {
            if (requestDTO.getTitle() == null && requestDTO.getContent() != null) {
                requestDTO.setTitle("无标题草稿");
            }

            ArticleDetailsDTO savedDraft = articleService.saveDraft(null, requestDTO, user.getId());
            return ResponseEntity.ok(savedDraft);
        }
    }

    @GetMapping("/article/drafts")
    @Operation(summary = "获取草稿列表", description = "获取当前用户的草稿列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Page<ArticleExcerptDTO>> getDrafts(
            @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "1") long pageNum,
            @Parameter(description = "每页数量") @RequestParam(name = "size", defaultValue = "10") long pageSize,
            @AuthenticationPrincipal Principal principal) {
        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Page<ArticleExcerptDTO> drafts = articleService.getProfilePageArticle(user.getId(), pageNum, pageSize, true);
        return ResponseEntity.ok(drafts);
    }

}
