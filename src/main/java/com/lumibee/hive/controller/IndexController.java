package com.lumibee.hive.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.TagService;
import com.lumibee.hive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "首页管理", description = "首页相关的 API 接口")
public class IndexController {

    @Autowired private ArticleService articleService;
    @Autowired private TagService tagService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private UserService userService;

    /**
     * 根路径处理 - 返回简单的成功响应
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootPath() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "LumiHive API Server");
        response.put("status", "running");
        return ResponseEntity.ok(response);
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
    @Operation(summary = "获取首页数据", description = "获取首页需要显示的文章、标签和精选文章数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Map<String, Object>> getHomeData(
            @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "1") long pageNum,
            @Parameter(description = "每页数量") @RequestParam(name = "size", defaultValue = "8") long pageSize) {
        
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

    @GetMapping("/api/index/articles")
    @Operation(summary = "获取首页文章", description = "获取首页需要显示的文章数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Map<String, Object>> getIndexArticles(
            @Parameter(description = "限制返回数量") @RequestParam(defaultValue = "6") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取热门文章
            List<ArticleExcerptDTO> popularArticles = articleService.selectArticleSummaries(limit);
            response.put("popularArticles", popularArticles);
            
            // 获取精选文章
            List<ArticleExcerptDTO> featuredArticles = articleService.selectFeaturedArticles();
            response.put("featuredArticles", featuredArticles);
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取文章失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
