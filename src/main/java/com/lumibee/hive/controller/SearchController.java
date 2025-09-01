package com.lumibee.hive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.service.ArticleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/search")
@Tag(name = "搜索管理", description = "搜索相关的 API 接口")
public class SearchController {

    @Autowired ArticleRepository articleRepository;

    @GetMapping
    @Operation(summary = "搜索文章", description = "根据查询关键词搜索文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功")
    })
    public ResponseEntity<List<ArticleDocument>> search(
            @Parameter(description = "搜索查询关键词") @RequestParam("query") String query) {
        // 使用前缀匹配搜索方法
        List<ArticleDocument> searchResults = articleRepository.findByTitleOrContentWithPrefix(query);

        return ResponseEntity.ok(searchResults);
    }
}