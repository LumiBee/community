package com.lumibee.hive.service;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.*;
import com.lumibee.hive.model.User;

public interface ArticleService {
    Page<ArticleExcerptDTO> getHomepageArticle(long pageNum, long pageSize);

    Page<ArticleExcerptDTO> getProfilePageArticle(long userId, long pageNum, long pageSize, String keyword);

    List<DraftDTO> getDraftPageArticle(long userId);

    List<ArticleExcerptDTO> getPopularArticles(int limit);

    List<ArticleExcerptDTO> getFeaturedArticles();

    ArticleDetailsDTO getArticleBySlug(String slug);

    ArticleDetailsDTO getArticleBySlug(String slug, Long userId, String ipAddress);

    ArticleDetailsDTO getArticleById(int articleId);

    List<ArticleExcerptDTO> getArticlesByTagSlug(String tagSlug);

    List<ArticleExcerptDTO> getArticlesByPortfolioId(int id);

    LikeResponse toggleLike(long userId, int articleId);

    boolean isUserLiked(long userId, int articleId);

    ArticleDetailsDTO publishArticle(ArticlePublishRequestDTO requestDTO, long userId);

    int countArticlesByUserId(long id);

    ArticleDetailsDTO saveDraft(Integer articleId, ArticlePublishRequestDTO requestDTO, Long userId);

    ArticleDetailsDTO selectDraftById(int articleId);

    ArticleDetailsDTO updateArticle(int articleId, ArticlePublishRequestDTO requestDTO, long userId);

    ArticleDetailsDTO deleteArticleById(int articleId, long userId);

    List<ArticleDetailsDTO> selectAll();

    int getFavoriteCount(int articleId);

    void setArticleFeatured(int articleId, boolean isFeatured, User.UserRole role);

    LocalDateTime getArticleGmtModified(int articleId);
}
