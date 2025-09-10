package com.lumibee.hive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.model.Tag;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "文章标签相关的 API 接口")
public class TagController {

    @Autowired
    private TagService tagService;
    
    @Autowired
    private ArticleService articleService;

    @GetMapping
    @Operation(summary = "获取所有标签", description = "获取系统中所有可用的标签")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.selectAllTags();
        return ResponseEntity.ok(tags);
    }
    
    @GetMapping("/{tagSlug}/articles")
    @Operation(summary = "根据标签slug获取文章列表", description = "根据标签slug获取该标签下的所有文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "标签不存在")
    })
    public ResponseEntity<List<ArticleExcerptDTO>> getArticlesByTagSlug(
            @Parameter(description = "标签slug") @PathVariable String tagSlug) {
        List<ArticleExcerptDTO> articles = articleService.getArticlesByTagSlug(tagSlug);
        return ResponseEntity.ok(articles);
    }

}
