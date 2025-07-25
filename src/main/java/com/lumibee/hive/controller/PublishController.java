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

import java.security.Principal;

@RestController
@RequestMapping("/api/article")
public class PublishController {

    @Autowired private UserService userService;
    @Autowired private ArticleService articleService;

    @PostMapping("/publish")
    public ResponseEntity<ArticleDetailsDTO> publishArticle(@AuthenticationPrincipal Principal principal,
                                                            @RequestBody ArticlePublishRequestDTO requestDTO) {

        Long userId = userService.getCurrentUserFromPrincipal(principal).getId();

        ArticleDetailsDTO newArticle = articleService.publishArticle(requestDTO, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newArticle);
    }

    @PutMapping("/{articleId}/edit")
    public ResponseEntity<ArticleDetailsDTO> editArticle(@PathVariable Integer articleId,
                                                         @AuthenticationPrincipal Principal principal,
                                                         @RequestBody ArticlePublishRequestDTO requestDTO) {
        Long userId = userService.getCurrentUserFromPrincipal(principal).getId();

        ArticleDetailsDTO updatedArticle = articleService.updateArticle(articleId, requestDTO, userId);

        return ResponseEntity.ok(updatedArticle);
    }

    @DeleteMapping("/delete/{articleId}")
    public ResponseEntity<ArticleDetailsDTO> deleteArticle(@PathVariable Integer articleId) {
        ArticleDetailsDTO deletedArticle = articleService.deleteArticleById(articleId);

        if (deletedArticle != null) {
            return ResponseEntity.ok(deletedArticle);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
