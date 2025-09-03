package com.lumibee.hive.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.model.ArticleDocument;

public interface ArticleService {
    Page<ArticleExcerptDTO> getHomepageArticle(long pageNum, long pageSize);
    Page<ArticleExcerptDTO> getProfilePageArticle(long userId, long pageNum, long pageSize);
    String createUniqueSlug(String title);
    ArticleDetailsDTO getArticleBySlug(String slug);
    ArticleDetailsDTO getArticleByIdSimple(Integer articleId);
    LikeResponse toggleLike(long userId, int articleId);
    boolean isUserLiked(long userId, int articleId);
    void incrementViewCount(Integer articleId);
    List<ArticleExcerptDTO> selectArticleSummaries(int limit);
    List<ArticleExcerptDTO> getArticlesByTagId(int tagId);
    ArticleDetailsDTO publishArticle(ArticlePublishRequestDTO requestDTO, Long userId);
    List<ArticleExcerptDTO> selectArticlesByPortfolioId(Integer id);
    Integer countArticlesByUserId(Long id);
    List<ArticleExcerptDTO> getArticlesByUserId(Long id);
    List<ArticleExcerptDTO> selectFeaturedArticles();
    ArticleDetailsDTO saveDraft(Integer articleId,ArticlePublishRequestDTO requestDTO, Long userId);
    Page<ArticleExcerptDTO> getArticlesByUserId(Long userId, long pageNum, long pageSize);
    ArticleDetailsDTO selectDraftById(Integer articleId);
    ArticleDetailsDTO updateArticle(Integer articleId, ArticlePublishRequestDTO requestDTO, Long userId);
    ArticleDetailsDTO deleteArticleById(Integer articleId, Long userId);
    List<ArticleDetailsDTO> selectAll();
    List<ArticleDocument> selectArticles(String query);
    List<ArticleDocument> selectRelatedArticles(ArticleDetailsDTO currentArticle, int limit);
    int getFavoriteCount(Integer articleId);
    void setArticleFeatured(Integer articleId, boolean isFeatured);
    void batchSetArticleFeatured(List<Integer> articleIds, boolean isFeatured);
}
