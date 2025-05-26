package com.lumibee.hive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.model.Article;

import java.util.List;

public interface ArticleService {
    Page<Article> getHomepageArticle(long pageNum, long pageSize);
    String createUniqueSlug(String title);
    Article publishArticle(Article article, List<String> tagsName, String portfolioName);
    Article getArticleBySlug(String slug);
    LikeResponse toggleLike(long userId, int articleId);
    boolean isUserLiked(long userId, int articleId);
    void incrementViewCount(Integer articleId);
    List<Article> getTopArticles();
}
