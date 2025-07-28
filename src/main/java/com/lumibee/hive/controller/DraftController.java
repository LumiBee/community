package com.lumibee.hive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class DraftController {
    @Autowired private ArticleService articleService;
    @Autowired private UserService userService;

    @PostMapping("/api/article/save-draft")
    @ResponseBody
    public ResponseEntity<?> saveDraft(@RequestBody ArticlePublishRequestDTO requestDTO,
                                       @AuthenticationPrincipal Principal principal) throws Exception {
        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
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
    public String showDraftsPage(Model model,
                                 @AuthenticationPrincipal Principal principal) {
        User user = userService.getCurrentUserFromPrincipal(principal);
        Page<ArticleExcerptDTO> drafts = articleService.getArticlesByUserId(user.getId(), 1, 10);

        model.addAttribute("drafts", drafts);
        return "drafts";
    }
}
