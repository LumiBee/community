package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.FavoriteDetailsDTO;
import com.lumibee.hive.dto.FavoriteResponse;
import com.lumibee.hive.mapper.ArticleFavoritesMapper;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.mapper.FavoriteMapper;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.ArticleFavorites;
import com.lumibee.hive.model.Favorites;
import com.lumibee.hive.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ArticleFavoritesMapper articleFavoritesMapper;

    @Override
    @Transactional
    public Favorites createFavorite(String favoriteName, Long userId) {
        if (favoriteName == null || favoriteName.isEmpty()) {
            return null; // 如果传入的favoriteName为空，直接返回null
        }

        Favorites favorite = new Favorites();
        favorite.setName(favoriteName);
        favorite.setGmtCreate(LocalDateTime.now());
        favorite.setGmtModified(LocalDateTime.now());
        favorite.setUserId(userId);
        favorite.setPublic(true); // 默认设置为公开
        favoriteMapper.insert(favorite);

        return favorite;
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteDetailsDTO selectFavoritesById(Long favoriteId) {
        // 1. 获取收藏夹基本信息
        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", favoriteId).eq("deleted", 0);
        Favorites favorite = favoriteMapper.selectOne(queryWrapper);

        if (favorite == null) {
            return null; // 如果收藏夹不存在，直接返回null
        }

        // 2. 获取该收藏夹下的所有文章摘要
        List<ArticleExcerptDTO> articles = articleFavoritesMapper.selectArticlesByFavoriteId(favorite.getId());

        // 3. 收集所有需要查询的用户ID
        // 首先添加收藏夹创建者的ID
        Set<Long> userIdsToFetch = new HashSet<>();
        if (favorite.getUserId() != null) {
            userIdsToFetch.add(favorite.getUserId());
        }
        // 然后添加所有文章作者的ID
        articles.stream()
                .map(ArticleExcerptDTO::getUserId)
                .filter(Objects::nonNull)
                .forEach(userIdsToFetch::add);

        // 4. 一次性批量查询所有用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIdsToFetch.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIdsToFetch);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));
        }

        // 5. 组装返回的 DTO (FavoriteDetailsDTO)
        FavoriteDetailsDTO favoriteDetailsDTO = new FavoriteDetailsDTO();
        BeanUtils.copyProperties(favorite, favoriteDetailsDTO);
        favoriteDetailsDTO.setArticlesCount(articles.size()); // 直接用列表大小，避免再次查询数据库

        // 5.1 设置收藏夹创建者的信息
        if (favorite.getUserId() != null) {
            User owner = userMap.get(favorite.getUserId());
            if (owner != null) {
                favoriteDetailsDTO.setUserName(owner.getUsername());
                favoriteDetailsDTO.setAvatarUrl(owner.getAvatarUrl());
            }
        }

        // 5.2 遍历文章列表，设置每篇文章的作者信息
        for (ArticleExcerptDTO article : articles) {
            if (article.getUserId() != null) {
                User author = userMap.get(article.getUserId());
                if (author != null) {
                    article.setUserName(author.getUsername());
                    article.setAvatarUrl(author.getAvatarUrl());
                }
            }
        }
        favoriteDetailsDTO.setArticles(articles);

        return favoriteDetailsDTO;
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
            favoriteDTO.setAvatarUrl(userMapper.selectById(favorite.getUserId()).getAvatarUrl());
            favoriteDTO.setUserName(userMapper.selectById(favorite.getUserId()).getUsername());
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
        queryWrapper.eq("article_id", articleId)
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
        Favorites newFavorite = createFavorite(favoriteName, userId);
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

    @Override
    @Transactional
    public Map<String, Object> removeFolderFromFavorite(Long userId, Integer favoriteId) {
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.selectById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        Favorites favorite = favoriteMapper.selectById(favoriteId);
        if (favorite == null || !favorite.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "收藏夹不存在或不属于当前用户");
            return result;
        }

        QueryWrapper<ArticleFavorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("favorite_id", favoriteId);
        articleFavoritesMapper.delete(queryWrapper);
        favoriteMapper.removeById(favorite.getId());

        result.put("success", true);
        result.put("message", "收藏夹已成功删除");

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> updateFavoriteFolder(Long userId, Integer favoriteId, String newName) {
        Map<String, Object> result = new HashMap<>();
        
        if (newName == null || newName.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "收藏夹名称不能为空");
            return result;
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        Favorites favorite = favoriteMapper.selectById(favoriteId);
        if (favorite == null || !favorite.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "收藏夹不存在或不属于当前用户");
            return result;
        }

        // 更新收藏夹名称
        favorite.setName(newName.trim());
        favorite.setGmtModified(LocalDateTime.now());
        favoriteMapper.updateById(favorite);

        result.put("success", true);
        result.put("message", "收藏夹名称已成功更新");
        result.put("favorite", favorite);

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> removeArticleFromFavorite(Long userId, Integer articledId, Long favoriteId) {
        Map<String, Object> result = new HashMap<>();

        Integer deleted = articleFavoritesMapper.deleteByArticleIdAndFavoriteId(articledId, favoriteId, userId);

        if (deleted > 0) {
            result.put("success", true);
            result.put("message", "已成功从该收藏夹中移除文章");
        } else {
            result.put("success", false);
            result.put("message", "未找到对应的收藏记录或移除失败");
        }

        result.put("isFavorited", false);

        return result;
    }
}
