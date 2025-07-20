package com.lumibee.hive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteResponse {
    private boolean success;
    private String message;
    private boolean favorited;
    private Integer favoriteCount;
}
