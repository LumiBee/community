package com.lumibee.hive.constant;

/**
 * 缓存名称常量类
 * 
 * 统一管理所有缓存名称，避免硬编码和命名不一致问题
 * 命名规范：使用 kebab-case 格式，语义清晰
 */
public class CacheNames {
    
    // ==================== 文章相关缓存 ====================
    
    /**
     * 文章详情缓存
     */
    public static final String ARTICLE_DETAIL = "article-detail";
    
    /**
     * 首页文章列表缓存
     */
    public static final String HOMEPAGE_ARTICLES = "homepage-articles";
    
    /**
     * 所有文章列表缓存
     */
    public static final String ALL_ARTICLES = "all-articles";
    
    /**
     * 标签文章列表缓存
     */
    public static final String TAG_ARTICLES = "tag-articles";
    
    /**
     * 用户文章列表缓存
     */
    public static final String USER_ARTICLES = "user-articles";
    
    /**
     * 搜索文章列表缓存
     */
    public static final String SEARCH_ARTICLES = "search-articles";
    
    /**
     * 热门文章列表缓存
     */
    public static final String POPULAR_ARTICLES = "popular-articles";
    
    /**
     * 精选文章列表缓存
     */
    public static final String FEATURED_ARTICLES = "featured-articles";
    
    /**
     * 作品集文章列表缓存
     */
    public static final String PORTFOLIO_ARTICLES = "portfolio-articles";
    
    // ==================== 用户相关缓存 ====================
    
    /**
     * 用户个人中心缓存
     */
    public static final String USER_PROFILE = "user-profile";
    
    /**
     * 用户关注关系缓存
     */
    public static final String USER_FOLLOW = "user-follow";
    
    /**
     * 用户统计信息缓存（粉丝数、关注数等）
     */
    public static final String USER_STATUS = "user-status";
    
    // ==================== 标签相关缓存 ====================
    
    /**
     * 所有标签列表缓存
     */
    public static final String ALL_TAGS = "all-tags";
    
    /**
     * 热门标签列表缓存
     */
    public static final String POPULAR_TAGS = "popular-tags";
    
    // ==================== 收藏相关缓存 ====================
    
    /**
     * 收藏详情缓存
     */
    public static final String FAVORITES_DETAIL = "favorites-detail";
    
    /**
     * 用户收藏列表缓存
     */
    public static final String FAVORITES_USER = "favorites-user";
    
    // ==================== 评论相关缓存 ====================
    
    /**
     * 文章评论列表缓存
     */
    public static final String COMMENTS = "comments";
    
    // ==================== 作品集相关缓存 ====================
    
    /**
     * 作品集详情缓存
     */
    public static final String PORTFOLIO_DETAIL = "portfolio-detail";
    
}
