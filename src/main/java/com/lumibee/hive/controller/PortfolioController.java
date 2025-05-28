package com.lumibee.hive.controller;

import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Portfolio;
import com.lumibee.hive.model.Tag;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.PortfolioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PortfolioController {

    @Autowired
    private PortfolioMapper portfolioMapper;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private ArticleService articleService;

    @GetMapping("/api/portfolio/{slug}")
    public ResponseEntity<List<Article>> getPortfolio(@PathVariable("slug") String slug) {
        // 根据 slug 获取 Portfolio
        List<Article> articles;

        if ("all".equals(slug)) {
            articles = articleService.getArticlesLimit(100);
        } else {

            articles = articleService.getArticlesLimit(100);
        }

        return ResponseEntity.ok(articles);
    }

}
