package com.lumibee.hive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.model.Article;

import java.util.List;

public interface ArticleService {
    Page<Article> getHomepageArticle(long pageNum, long pageSize);
    String createUniqueSlug(String title);
    Article publishArticle(Article article, List<String> tagsName);
    Article getArticleBySlug(String slug);
}
