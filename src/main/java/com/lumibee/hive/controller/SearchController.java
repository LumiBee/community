package com.lumibee.hive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.service.ArticleRepository;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired ArticleRepository articleRepository;

    @GetMapping
    public ResponseEntity<List<ArticleDocument>> search(@RequestParam("query") String query) {
        // 使用前缀匹配搜索方法
        List<ArticleDocument> searchResults = articleRepository.findByTitleOrContentWithPrefix(query);

        return ResponseEntity.ok(searchResults);
    }
}