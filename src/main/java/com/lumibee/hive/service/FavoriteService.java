package com.lumibee.hive.service;

import com.lumibee.hive.dto.FavoriteDetailsDTO;
import com.lumibee.hive.dto.FavoriteResponse;
import com.lumibee.hive.model.Favorites;

import java.util.List;
import java.util.Map;

public interface FavoriteService {
    Favorites createFavorite(String favoriteName, Long userId);
    FavoriteDetailsDTO selectFavoritesById(Long favoriteId);
    List<FavoriteDetailsDTO> getFavoritesByUserId(Long userId);
    FavoriteResponse addArticleToFavorite(Long userId, Integer articleId, Long favoriteId);
    FavoriteResponse createFavoriteAndAddArticle(Long userId, Integer articleId, String favoriteName);
    Map<String, Object> removeAllArticlesFromFavorite(Long userId, Integer articleId);
    Map<String, Object> removeFolderFromFavorite(Long userId, Integer favoriteId);
    Map<String, Object> updateFavoriteFolder(Long userId, Integer favoriteId, String newName);
}
