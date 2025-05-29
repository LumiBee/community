package com.lumibee.hive.controller;

import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired private TagService tagService;
    @Autowired private ArticleService articleService;

    @GetMapping("/{slug}")
    public ResponseEntity<List<ArticleExcerptDTO>> getArticlesByTag(@PathVariable("slug") String slug) {
        List<ArticleExcerptDTO> articles;

        if ("all".equals(slug)) {
            articles = articleService.selectArticleSummaries(100);
        } else {
            TagDTO tag = tagService.selectTagBySlug(slug);
            if (tag == null) {
                return ResponseEntity.notFound().build();
            }
            articles = articleService.getArticlesByTagId(tag.getTagId());
        }

        return ResponseEntity.ok(articles);
    }
}
