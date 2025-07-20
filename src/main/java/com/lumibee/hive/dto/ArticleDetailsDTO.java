package com.lumibee.hive.dto;

import com.lumibee.hive.model.Article;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailsDTO {
    private Integer articleId;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private Article.ArticleStatus status;
    private Integer portfolioId;
    private LocalDateTime gmtModified;
    private Integer viewCount;
    private Integer likes;
    private Integer favoriteCount;

    private Long userId;
    private String userName;
    private String avatarUrl;
    private PortfolioDTO portfolio;
    private List<TagDTO> tags;

    private boolean isLiked;
    private boolean isFollowed;
    private boolean isFavorited;
}
