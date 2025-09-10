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
        return String.join(SEPARATOR, ARTICLES_PREFIX, "list", "all");
    }
    
    // 首页文章列表
    public static String homepageArticles(int page, int size) {
        return String.join(SEPARATOR, 
            ARTICLES_PREFIX, "list", "homepage", 
            "page" + PARAM_SEPARATOR + page,
            "size" + PARAM_SEPARATOR + size
        );
    }
    
    // 标签文章列表
    public static String tagArticles(String tagSlug) {
        return String.join(SEPARATOR,
            ARTICLES_PREFIX, "list", "tag", tagSlug
        );
    }
    
    // 用户文章列表
    public static String userArticles(Long userId) {
        return String.join(SEPARATOR,
            ARTICLES_PREFIX, "list", "user", userId.toString()
        );
    }
    
    // 搜索文章列表
    public static String searchArticles(String query) {
        String queryHash = String.valueOf(query.hashCode());
        return String.join(SEPARATOR,
            ARTICLES_PREFIX, "list", "search", queryHash
        );
    }
    
    // 热门文章列表
    public static String popularArticles(Integer limit) {
        return String.join(SEPARATOR,
            ARTICLES_PREFIX, "list", "popular", limit.toString()
        );
    }
    
    // 精选文章列表
    public static String featuredArticles() {
        return String.join(SEPARATOR,
            ARTICLES_PREFIX, "list", "featured"
        );
    }
    
    // 作品集文章列表
    public static String portfolioArticles(Long portfolioId) {
        return String.join(SEPARATOR,
            ARTICLES_PREFIX, "list", "portfolio", portfolioId.toString()
        );
    }
    
    // 文章详情
    public static String articleDetail(String slug) {
        return String.join(SEPARATOR,
            ARTICLES_PREFIX, "detail", slug
        );
    }
    
    // ==================== 用户相关缓存键 ====================

    // 用户个人中心
    public static String userProfile(String name) {
        return String.join(SEPARATOR,
                USERS_PREFIX, "profile", name
        );
    }
    
    // 用户关注关系
    public static String userFollow(Long userId, Long followerId) {
        return String.join(SEPARATOR,
            USERS_PREFIX, "follow", userId.toString(), followerId.toString()
        );
    }
    
    // 用户粉丝数
    public static String userFansCount(Long userId) {
        return String.join(SEPARATOR,
            USERS_PREFIX, "count", "fans", userId.toString()
        );
    }
    
    // 用户关注数
    public static String userFollowersCount(Long userId) {
        return String.join(SEPARATOR,
            USERS_PREFIX, "count", "followers", userId.toString()
        );
    }
    
    // ==================== 标签相关缓存键 ====================
    
    // 所有标签列表
    public static String allTags() {
        return String.join(SEPARATOR, TAGS_PREFIX, "list", "all");
    }
    
    // ==================== 收藏相关缓存键 ====================
    
    // 收藏详情
    public static String favoriteDetail(Long favoriteId) {
        return String.join(SEPARATOR,
            FAVORITES_PREFIX, "detail", favoriteId.toString()
        );
    }
    
    // 用户收藏列表
    public static String userFavorites(Long userId) {
        return String.join(SEPARATOR,
            FAVORITES_PREFIX, "list", "user", userId.toString()
        );
    }
    
    // ==================== 评论相关缓存键 ====================
    
    // 文章评论列表
    public static String articleComments(Long articleId) {
        return String.join(SEPARATOR,
            COMMENTS_PREFIX, "list", "article", articleId.toString()
        );
    }
    
    // ==================== 作品集相关缓存键 ====================
    
    // 作品集详情
    public static String portfolioDetail(Integer portfolioId) {
        return String.join(SEPARATOR,
            PORTFOLIOS_PREFIX, "detail", portfolioId.toString()
        );
    }
}