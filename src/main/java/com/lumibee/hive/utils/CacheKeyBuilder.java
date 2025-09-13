package com.lumibee.hive.utils;

import org.springframework.stereotype.Component;

/**
 * 缓存键构建工具类
 */
@Component
public class CacheKeyBuilder {
    
    private static final String ARTICLES_PREFIX = "articles";
    private static final String USERS_PREFIX = "users";
    private static final String TAGS_PREFIX = "tags";
    private static final String FAVORITES_PREFIX = "favorites";
    private static final String COMMENTS_PREFIX = "comments";
    private static final String PORTFOLIOS_PREFIX = "portfolios";
    
    private static final String SEPARATOR = "::";
    private static final String PARAM_SEPARATOR = ":";
    
    // ==================== 文章相关缓存键 ====================
    
    // 所有文章列表
    public static String allArticles() {
        return "";
    }
    
    // 首页文章列表
    public static String homepageArticles(int page, int size) {
        return String.join(SEPARATOR, 
            "page" + PARAM_SEPARATOR + page,
            "size" + PARAM_SEPARATOR + size
        );
    }
    
    // 标签文章列表
    public static String tagArticles(String tagSlug) {
        return tagSlug;
    }
    
    // 用户文章列表
    public static String userArticles(Long userId) {
        return userId.toString();
    }
    
    // 搜索文章列表
    public static String searchArticles(String query) {
        String queryHash = String.valueOf(query.hashCode());
        return queryHash;
    }
    
    // 热门文章列表
    public static String popularArticles(Integer limit) {
        return limit.toString();
    }
    
    // 精选文章列表
    public static String featuredArticles() {
        return "";
    }
    
    // 作品集文章列表
    public static String portfolioArticles(Long portfolioId) {
        return portfolioId.toString();
    }
    
    // 文章详情
    public static String articleDetail(String slug) {
        return slug;
    }
    
    // ==================== 用户相关缓存键 ====================

    // 用户个人中心
    public static String userProfile(String name) {
        return name;
    }
    
    // 用户个人中心（根据ID）
    public static String userProfileById(Long userId) {
        return "id::" + userId.toString();
    }
    
    // 用户关注关系
    public static String userFollow(Long userId, Long followerId) {
        return userId.toString() + "::" + followerId.toString();
    }
    
    // 用户粉丝数
    public static String userFansCount(Long userId) {
        return "fans::" + userId.toString();
    }
    
    // 用户关注数
    public static String userFollowersCount(Long userId) {
        return "followers::" + userId.toString();
    }
    
    // ==================== 标签相关缓存键 ====================
    
    // 所有标签列表
    public static String allTags() {
        return "";
    }
    
    // 标签详情
    public static String tagDetail(String slug) {
        return slug;
    }
    
    // 文章标签列表
    public static String articleTags(Integer articleId) {
        return articleId.toString();
    }
    
    // ==================== 收藏相关缓存键 ====================
    
    // 收藏详情
    public static String favoriteDetail(Long favoriteId) {
        return favoriteId.toString();
    }
    
    // 用户收藏列表
    public static String userFavorites(Long userId) {
        return userId.toString();
    }
    
    // ==================== 评论相关缓存键 ====================
    
    // 文章评论列表
    public static String articleComments(Long articleId) {
        return articleId.toString();
    }
    
    // ==================== 作品集相关缓存键 ====================
    
    // 作品集详情
    public static String portfolioDetail(Integer portfolioId) {
        return portfolioId.toString();
    }
}