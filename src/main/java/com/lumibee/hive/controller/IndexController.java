package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.TagService;
import com.lumibee.hive.service.UserService;

@RestController
public class IndexController {

    @Autowired private ArticleService articleService;
    @Autowired private TagService tagService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private UserService userService;

    /**
     * 重定向到Vue SPA
     */
    @GetMapping("/")
    public ResponseEntity<Void> redirectToSPA() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/")
                .build();
    }

    /**
     * 获取发布页面数据API
     */
    @GetMapping("/api/publish")
    public ResponseEntity<Map<String, Object>> getPublishData(
            @RequestParam(name = "draftId", required = false) Integer draftId) {
        Map<String, Object> response = new HashMap<>();
        
        if (draftId != null) {
            try {
                ArticleDetailsDTO draft = articleService.selectDraftById(draftId);
                if (draft != null) {
                    response.put("draft", draft);
                }
            } catch (Exception e) {
                // 草稿不存在时返回空对象
            }
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取首页数据API
     */
    @GetMapping("/api/home")
    public ResponseEntity<Map<String, Object>> getHomeData(
            @RequestParam(name = "page", defaultValue = "1") long pageNum,
            @RequestParam(name = "size", defaultValue = "8") long pageSize) {
        
        int limit = 6;
        
        Page<ArticleExcerptDTO> articlePage = articleService.getHomepageArticle(pageNum, pageSize);
        List<ArticleExcerptDTO> popularArticles = articleService.selectArticleSummaries(limit);
        List<ArticleExcerptDTO> featuredArticles = articleService.selectFeaturedArticles();
        List<TagDTO> allTags = tagService.selectAllTags();

        Map<String, Object> response = new HashMap<>();
        response.put("articles", articlePage);
        response.put("popularArticles", popularArticles);
        response.put("tags", allTags);
        response.put("featuredArticles", featuredArticles);

        return ResponseEntity.ok(response);
    }

}
