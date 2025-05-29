package com.lumibee.hive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.model.Article;

import java.util.List;

public interface ArticleService {
    Page<ArticleExcerptDTO> getHomepageArticle(long pageNum, long pageSize);
    String createUniqueSlug(String title);
    ArticleDetailsDTO getArticleBySlug(String slug);
    LikeResponse toggleLike(long userId, int articleId);
    boolean isUserLiked(long userId, int articleId);
    void incrementViewCount(Integer articleId);
    List<ArticleExcerptDTO> selectArticleSummaries(int limit);
    List<ArticleExcerptDTO> getArticlesByTagId(int tagId);
    ArticleDetailsDTO publishArticle(ArticlePublishRequestDTO requestDTO, Long userId);
}
