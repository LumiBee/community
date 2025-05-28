package com.lumibee.hive.controller;

import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Tag;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private ArticleService articleService;

    @GetMapping("/api/tags/{slug}")
    public ResponseEntity<List<Article>> getArticlesByTag(@PathVariable("slug") String slug) {
        List<Article> articles;

        if ("all".equals(slug)) {
            articles = articleService.getArticlesLimit(100);
        } else {
            Tag tag = tagService.selectTagBySlug(slug);
            if (tag == null) {
                return ResponseEntity.notFound().build();
            }
            articles = articleService.getArticlesByTagId(tag.getTagId());
        }

        return ResponseEntity.ok(articles);
    }
}
