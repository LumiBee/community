package com.lumibee.hive.dto;

import lombok.Data;

@Data
public class FavoriteRequestDTO {
    private Integer articleId;
    private String favoriteName;
    private Long favoriteId;
}
