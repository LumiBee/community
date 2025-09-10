package com.lumibee.hive.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.model.User;

public interface ArticleService {
    Page<ArticleExcerptDTO> getHomepageArticle(long pageNum, long pageSize);
    Page<ArticleExcerptDTO> getProfilePageArticle(long userId, long pageNum, long pageSize);
    List<ArticleExcerptDTO> getPopularArticles(int limit);
    List<ArticleExcerptDTO> getFeaturedArticles();
    ArticleDetailsDTO getArticleBySlug(String slug);
    List<ArticleExcerptDTO> getArticlesByTagSlug(String tagSlug);
    List<ArticleExcerptDTO> getArticlesByPortfolioId(Integer id);
    LikeResponse toggleLike(long userId, int articleId);
    boolean isUserLiked(long userId, int articleId);
    void incrementViewCount(Integer articleId);
    ArticleDetailsDTO publishArticle(ArticlePublishRequestDTO requestDTO, Long userId);
    Integer countArticlesByUserId(Long id);
    ArticleDetailsDTO saveDraft(Integer articleId,ArticlePublishRequestDTO requestDTO, Long userId);
    ArticleDetailsDTO selectDraftById(Integer articleId);
    ArticleDetailsDTO updateArticle(Integer articleId, ArticlePublishRequestDTO requestDTO, Long userId);
    ArticleDetailsDTO deleteArticleById(Integer articleId, Long userId);
    List<ArticleDetailsDTO> selectAll();
    int getFavoriteCount(Integer articleId);
    void setArticleFeatured(Integer articleId, boolean isFeatured, User.UserRole role);
}
