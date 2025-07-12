package com.lumibee.hive.controller;

import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.service.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired ArticleRepository articleRepository;

    @GetMapping
    public ResponseEntity<List<ArticleDocument>> search(@RequestParam("query") String query) {
        // 使用新的前缀匹配搜索方法
        List<ArticleDocument> searchResults = articleRepository.findByTitleOrContentWithPrefix(query);

        return ResponseEntity.ok(searchResults);
    }
}