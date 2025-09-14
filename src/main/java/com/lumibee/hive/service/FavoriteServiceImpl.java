package com.lumibee.hive.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.FavoriteDetailsDTO;
import com.lumibee.hive.dto.FavoriteResponse;
import com.lumibee.hive.mapper.ArticleFavoritesMapper;
import com.lumibee.hive.mapper.FavoriteMapper;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.ArticleFavorites;
import com.lumibee.hive.model.Favorites;
import com.lumibee.hive.model.User;

/**
 * 收藏服务实现类
 * 负责收藏夹相关的业务逻辑处理，包括收藏夹的增删改查、文章收藏管理等
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ArticleFavoritesMapper articleFavoritesMapper;
    @Autowired private RedisClearCacheService redisClearCacheService;
    @Autowired private RedisMonitoringService redisMonitoringService;
    @Autowired private RedisCounterService redisCounterService;

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

        // 清除用户收藏相关缓存
        try {
            redisClearCacheService.clearUserFavoritesCaches(userId);
        } catch (Exception e) {
            System.err.println("清除用户收藏缓存时出错: " + e.getMessage());
        }

        return favorite;
    }

    @Override
    @Cacheable(value = "favorites::detail", key = "#favoriteId")
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
    @Cacheable(value = "favorites::list::user", key = "#userId")
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
            
            // 从 Redis 获取收藏夹文章数量
            try {
                int articlesCount = redisCounterService.getFavoriteArticleCount(favorite.getId());
                if (!redisCounterService.existsFavoriteArticleCount(favorite.getId())) {
                    // 如果 Redis 中没有数据，从数据库获取并设置到 Redis
                    int dbCount = articleFavoritesMapper.countArticlesInFavorite(favorite.getId());
                    redisCounterService.setFavoriteArticleCount(favorite.getId(), dbCount);
                    articlesCount = dbCount;
                }
                favoriteDTO.setArticlesCount(articlesCount);
            } catch (Exception e) {
                // 如果 Redis 出错，从数据库获取
                favoriteDTO.setArticlesCount(articleFavoritesMapper.countArticlesInFavorite(favorite.getId()));
            }
            
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

        // 更新 Redis 计数器
        try {
            // 文章被收藏次数 +1
            redisCounterService.incrementArticleFavorite(articleId);
            // 收藏夹文章数量 +1
            redisCounterService.incrementFavoriteArticle(favoriteId);
            // 用户收藏文章数量 +1
            redisCounterService.incrementUserFavorite(userId);
        } catch (Exception e) {
            System.err.println("更新 Redis 计数器时出错: " + e.getMessage());
        }

        // 清除用户收藏相关缓存
        try {
            redisClearCacheService.clearUserFavoritesCaches(userId);
        } catch (Exception e) {
            System.err.println("清除用户收藏缓存时出错: " + e.getMessage());
        }

        // 从 Redis 获取文章被收藏次数
        int articlesCount = redisCounterService.getArticleFavoriteCount(articleId);
        if (articlesCount == 0) {
            // 如果 Redis 中没有数据，从数据库获取
            articlesCount = articleFavoritesMapper.countArticlesFavorited(articleId);
            redisCounterService.setArticleFavoriteCount(articleId, articlesCount);
        }

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
            // 更新 Redis 计数器
            try {
                // 文章被收藏次数 -deletedRows
                redisCounterService.incrementBy("article:favorite:" + articleId, -deletedRows);
                // 用户收藏文章数量 -deletedRows
                redisCounterService.incrementBy("user:favorite:" + userId, -deletedRows);
            } catch (Exception e) {
                System.err.println("更新 Redis 计数器时出错: " + e.getMessage());
            }
            
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
            // 更新 Redis 计数器
            try {
                // 文章被收藏次数 -1
                redisCounterService.decrementArticleFavorite(articledId);
                // 收藏夹文章数量 -1
                redisCounterService.decrementFavoriteArticle(favoriteId);
                // 用户收藏文章数量 -1
                redisCounterService.decrementUserFavorite(userId);
            } catch (Exception e) {
                System.err.println("更新 Redis 计数器时出错: " + e.getMessage());
            }
            
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
