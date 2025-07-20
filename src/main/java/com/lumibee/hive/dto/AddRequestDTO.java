package com.lumibee.hive.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddRequestDTO {
    private Integer articleId;
    private String favoriteName;
    private Long favoriteId;
}
