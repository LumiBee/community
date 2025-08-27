package com.lumibee.hive.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lumibee.hive.model.Article;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("liked")
    private boolean isLiked;
    @JsonProperty("followed")
    private boolean isFollowed;
    @JsonProperty("favorited")
    private boolean isFavorited;
}
