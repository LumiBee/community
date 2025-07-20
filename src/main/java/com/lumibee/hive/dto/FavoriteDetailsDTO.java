package com.lumibee.hive.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FavoriteDetailsDTO {
    private Long id;
    private Long userId;
    private String name;
    private String slug;
    private String description;
    private LocalDateTime gmtModified;

    private String avatarUrl;
    private String userName;
    private List<ArticleExcerptDTO> articles;
    private Integer articlesCount;

    private boolean isPublic;
}
