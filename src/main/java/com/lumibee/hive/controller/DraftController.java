package com.lumibee.hive.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/api/article/save-draft")
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
            ArticleDetailsDTO updatedDraft = articleService.updateArticle(requestDTO.getArticleId(), requestDTO, user.getId());
            return ResponseEntity.ok(updatedDraft);
        }else {
            if (requestDTO.getTitle() == null || requestDTO.getContent() == null) {
                requestDTO.setTitle("无标题草稿");
            }

            ArticleDetailsDTO savedDraft = articleService.saveDraft(requestDTO, user.getId());
            return ResponseEntity.ok(savedDraft);
        }
    }

    @GetMapping("/drafts")
    @Operation(summary = "显示草稿页面", description = "显示用户的草稿列表页面")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "页面显示成功")
    })
    public String showDraftsPage(
            @Parameter(description = "页面模型") Model model,
            @AuthenticationPrincipal Principal principal) {
        User user = userService.getCurrentUserFromPrincipal(principal);
        Page<ArticleExcerptDTO> drafts = articleService.getArticlesByUserId(user.getId(), 1, 10);

        model.addAttribute("drafts", drafts);
        return "drafts";
    }
}
