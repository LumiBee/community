package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.FavoriteDetailsDTO;
import com.lumibee.hive.dto.FavoriteResponse;
import com.lumibee.hive.mapper.ArticleFavoritesMapper;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.mapper.FavoriteMapper;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.ArticleFavorites;
import com.lumibee.hive.model.Favorites;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ArticleMapper articleMapper;
    @Autowired private ArticleFavoritesMapper articleFavoritesMapper;

    @Override
    @Transactional
    public Favorites createFavorite(String favoriteName, String description, Long userId) {
        if (favoriteName == null || favoriteName.isEmpty()) {
            return null; // 如果传入的favoriteName为空，直接返回null
        }

        Favorites favorite = new Favorites();
        favorite.setName(favoriteName);
        favorite.setSlug(SlugGenerator.generateSlug(favoriteName));
        favorite.setDescription(description);
        favorite.setGmtCreate(LocalDateTime.now());
        favorite.setGmtModified(LocalDateTime.now());
        favorite.setUserId(userId);
        favorite.setPublic(true); // 默认设置为公开
        favoriteMapper.insert(favorite);


        return favorite;
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteDetailsDTO selectPortfolioBySlug(String slug) {
        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("slug", slug)
                    .eq("deleted", 0);
        Favorites favorite = favoriteMapper.selectOne(queryWrapper);
        if (favorite != null) {
            FavoriteDetailsDTO favoriteDetailsDTO = new FavoriteDetailsDTO();
            BeanUtils.copyProperties(favorite, favoriteDetailsDTO);
            favoriteDetailsDTO.setArticlesCount(articleFavoritesMapper.countArticlesInFavorite(favorite.getId()));

            // 获取用户信息
            if (favorite.getUserId() != null) {
                favoriteDetailsDTO.setUserName(userMapper.selectById(favorite.getUserId()).getUsername());
            }

            // 获取文章列表
            favoriteDetailsDTO.setArticles(articleFavoritesMapper.selectArticlesByFavoriteId(favorite.getId()));

            return favoriteDetailsDTO;
        }
        return null; // 如果没有找到对应的Favorites，返回null
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteDetailsDTO> getFavoritesByUserId(Long userId) {
        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                    .eq("deleted", 0)
                    .orderByDesc("gmt_create");
        List<Favorites> favoritesList = favoriteMapper.selectList(queryWrapper);

        if (favoritesList == null || favoritesList.isEmpty()) {
            return List.of();
        }
        return favoritesList.stream().map(favorite -> {
            FavoriteDetailsDTO favoriteDTO = new FavoriteDetailsDTO();
            BeanUtils.copyProperties(favorite, favoriteDTO);
            favoriteDTO.setArticlesCount(articleFavoritesMapper.countArticlesInFavorite(favorite.getId()));
            return favoriteDTO;
        }).toList();

    }

    @Override
    @Transactional
    public FavoriteResponse addArticleToFavorite(Long userId, Integer articleId, Long favoriteId) {
        Favorites favorite = favoriteMapper.selectById(favoriteId);
        if (favorite == null || !favorite.getUserId().equals(userId)) {
            return new FavoriteResponse(false, "收藏夹不存在或不属于当前用户", false, null);
        }

        QueryWrapper<ArticleFavorites> queryWrapper = new QueryWrapper<> ();
        queryWrapper.eq("user_id", userId)
                    .eq("article_id", articleId)
                    .eq("favorite_id", favoriteId);
        ArticleFavorites existingArticleFavorite = articleFavoritesMapper.selectOne(queryWrapper);
        if (existingArticleFavorite != null) {
            return new FavoriteResponse(false, "文章已在收藏夹中", true, null);
        }

        ArticleFavorites newArticleFavorite = new ArticleFavorites();
        newArticleFavorite.setUserId(userId);
        newArticleFavorite.setArticleId(articleId);
        newArticleFavorite.setFavoriteId(favoriteId);
        articleFavoritesMapper.insert(newArticleFavorite);

        Integer articlesCount = articleFavoritesMapper.countArticlesFavorited(articleId);

        return new FavoriteResponse(true, "文章成功收藏到文件夹", true, articlesCount);
    }

    @Override
    @Transactional
    public FavoriteResponse createFavoriteAndAddArticle(Long userId, Integer articleId, String favoriteName) {
        if (favoriteName == null || favoriteName.isBlank()) {
            return new FavoriteResponse(false, "收藏夹名称不能为空", false, null);
        }

        // 创建新的收藏夹
        Favorites newFavorite = createFavorite(favoriteName, null, userId);
        if (newFavorite == null) {
            return new FavoriteResponse(false, "创建收藏夹失败", false, null);
        }

        return addArticleToFavorite(userId, articleId, newFavorite.getId());
    }

    @Override
    @Transactional
    public Map<String, Object> removeAllArticlesFromFavorite(Long userId, Integer articleId) {
        Map<String, Object> result = new HashMap<>();

        int deletedRows = articleFavoritesMapper.deleteByArticleId(articleId, userId);

        if (deletedRows > 0) {
            result.put("success", true);
            result.put("message", "已成功从收藏夹中移除所有文章");
        } else {
            result.put("success", false);
            result.put("message", "未找到对应的收藏记录或移除失败");
        }

        result.put("isFavorited", false);

        return result;
    }
}
