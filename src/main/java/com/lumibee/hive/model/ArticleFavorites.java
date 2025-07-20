package com.lumibee.hive.model;

import lombok.Data;

@Data
public class ArticleFavorites {
    private Long favoriteId;
    private Integer articleId;
    private Long userId;
}
