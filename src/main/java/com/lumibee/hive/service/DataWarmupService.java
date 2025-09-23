package com.lumibee.hive.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.model.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DataWarmupService {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private TagService tagService;
    @Autowired
    private PortfolioService portfolioService;
    @Autowired
    private RedisCounterService redisCounterService;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisClearCacheService redisClearCacheService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupCaches() {
        log.info("开始预热缓存...");
        
        // 先清除所有缓存，确保从干净的状态开始
        log.info("清除所有缓存...");
        redisClearCacheService.clearAllCaches();

        // 只预热核心数据
        warmupCoreData();

        // 延迟预热其他数据
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10000); // 10秒后预热其他数据
                warmupOtherData();
            } catch (Exception e) {
                log.error("预热缓存失败: {}", e.getMessage(), e);
            }
        });
    }

    // 定时预热所有数据
    @Scheduled(cron = "0 0 7 * * ?") // 每天7点预热所有数据
    public void warmupAllData() {
        log.info("开始预热所有数据...");
        
        // 先清除所有缓存，确保从干净的状态开始
        log.info("清除所有缓存...");
        redisClearCacheService.clearAllCaches();
        
        warmupCoreData();
        warmupOtherData();
    }
    
    /**
     * 手动清除所有缓存并重新预热
     * 用于调试和紧急情况
     */
    public void clearAndWarmupAllCaches() {
        log.info("手动清除所有缓存并重新预热...");
        
        // 清除所有缓存
        redisClearCacheService.clearAllCaches();
        
        // 重新预热所有数据
        warmupCoreData();
        warmupOtherData();
        
        log.info("缓存清除和预热完成");
    }
    
    // 预热核心数据
    private void warmupCoreData() {
        log.info("预热核心数据...");
        warmupHomepageArticles();
        warmupPopularArticles();
        warmupFeaturedArticles();
        warmupTagList();
    }
    
    // 预热其他数据
    private void warmupOtherData() {
        log.info("预热其他数据...");
        warmupTagArticles();
        warmupPortfolioArticles();
        warmupArticleCounters();
        warmupElasticSearchIndex();
    }

    // 预热首页文章列表
    private void warmupHomepageArticles() {
        log.info("预热首页文章列表...");

        for (int page = 1; page <= 3; page++) {
            try {
                articleService.getHomepageArticle(page, 10);
                log.info("预热首页文章列表第{}页成功", page);
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("预热首页文章列表第{}页失败: {}", page, e.getMessage(), e);
            }
        }
    }

    // 预热热门文章
    private void warmupPopularArticles() {
        log.info("预热热门文章...");

        try {
            articleService.getPopularArticles(10);
            log.info("预热热门文章成功");
        } catch (Exception e) {
            log.error("预热热门文章失败: {}", e.getMessage(), e);
        }
    }

    // 预热精选文章
    private void warmupFeaturedArticles() {
        log.info("预热精选文章...");

        try {
            articleService.getFeaturedArticles();
            log.info("预热精选文章成功");
        } catch (Exception e) {
            log.error("预热精选文章失败: {}", e.getMessage(), e);
        }
    }

    // 预热标签列表
    private void warmupTagList() {
        log.info("预热标签列表...");

        try {
            tagService.selectAllTags();
            log.info("预热标签列表成功");
        } catch (Exception e) {
            log.error("预热标签列表失败: {}", e.getMessage(), e);
        }
    }

    // 预热标签文章列表
    private void warmupTagArticles() {
        log.info("预热标签文章列表...");

        try {
            List<TagDTO> tags = tagService.selectAllTags();
            for (TagDTO tag : tags) {
                try {
                    articleService.getArticlesByTagSlug(tag.getSlug());
                    log.info("预热标签文章列表 {} 成功", tag.getName());
                    Thread.sleep(500);
                } catch (Exception e) {
                    log.error("预热标签文章列表 {} 失败: {}", tag.getName(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("获取标签列表失败: {}", e.getMessage(), e);
        }
    }

    // 预热作品集列表
    private void warmupPortfolioArticles() {
        log.info("预热作品集文章列表...");

        try {
            List<PortfolioDetailsDTO> portfolios = portfolioService.selectAllPortfolios();
            for (PortfolioDetailsDTO portfolio : portfolios) {
                try {
                    articleService.getArticlesByPortfolioId(portfolio.getId());
                    log.info("预热作品集文章列表 {} 成功", portfolio.getName());
                    Thread.sleep(500);
                } catch (Exception e) {
                    log.error("预热作品集文章列表 {} 失败: {}", portfolio.getName(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("获取作品集列表失败: {}", e.getMessage(), e);
        }
    }

    // 预热文章计数器
    private void warmupArticleCounters() {
        log.info("预热文章计数器...");

        try {
            List<Article> articles = articleMapper.selectList(null);
            for (Article article : articles) {
                if (article.getViewCount() != null) {
                    redisCounterService.setArticleViewCount(article.getArticleId(),
                            article.getViewCount());
                }
                if (article.getLikes() != null) {
                    redisCounterService.setArticleLikeCount(article.getArticleId(),
                            article.getLikes());
                }
            }
            log.info("预热文章计数器成功");
        } catch (Exception e) {
            log.error("预热文章计数器失败: {}", e.getMessage(), e);
        }
    }

    // 预热Elastic Search文章索引
    private void warmupElasticSearchIndex() {
        log.info("预热Elastic Search文章索引...");

        List<ArticleDetailsDTO> articles = articleService.selectAll();
        if (articles == null || articles.isEmpty()) {
            return ;
        }

        // 批量查询用户信息，避免在循环中逐个查询
        List<Long> userIds = new ArrayList<>();
        for (Object articleObj : articles) {
            try {
                // 使用反射避免类型转换问题
                Long userId = (Long) articleObj.getClass().getMethod("getUserId").invoke(articleObj);
                if (userId != null && !userIds.contains(userId)) {
                    userIds.add(userId);
                }
            } catch (Exception e) {
                log.warn("获取文章用户ID失败: {}", e.getMessage());
            }
        }

        Map<Long, User> userMap = new HashMap<>();
        List<User> users = userService.selectByIds(userIds);
        if (users != null) {
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    userMap.put(user.getId(), user);
                }
            }
        }

        List<ArticleDocument> documents = new ArrayList<>();
        for (Object articleObj : articles) {
            try {
                // 使用反射获取文章信息
                Long userId = (Long) articleObj.getClass().getMethod("getUserId").invoke(articleObj);
                Integer articleId = (Integer) articleObj.getClass().getMethod("getArticleId").invoke(articleObj);
                String title = (String) articleObj.getClass().getMethod("getTitle").invoke(articleObj);
                String content = (String) articleObj.getClass().getMethod("getContent").invoke(articleObj);
                String excerpt = (String) articleObj.getClass().getMethod("getExcerpt").invoke(articleObj);
                String slug = (String) articleObj.getClass().getMethod("getSlug").invoke(articleObj);
                Integer likes = (Integer) articleObj.getClass().getMethod("getLikes").invoke(articleObj);
                Integer viewCount = (Integer) articleObj.getClass().getMethod("getViewCount").invoke(articleObj);
                Object gmtModifiedObj = articleObj.getClass().getMethod("getGmtModified").invoke(articleObj);
                String backgroundUrl = null;
                try {
                    backgroundUrl = (String) articleObj.getClass().getMethod("getBackgroundUrl").invoke(articleObj);
                } catch (NoSuchMethodException e) {
                    // ArticleDetailsDTO可能没有getBackgroundUrl方法，使用null
                    backgroundUrl = null;
                }
                
                User user = userMap.get(userId);

                ArticleDocument document = new ArticleDocument();
                document.setId(articleId);
                document.setTitle(title);
                document.setContent(content);
                document.setExcerpt(excerpt);
                document.setSlug(slug);
                document.setLikes(likes);
                document.setViewCount(viewCount);
                document.setUserName(user != null ? user.getName() : "");
                document.setAvatarUrl(user != null ? user.getAvatarUrl() : "");
                document.setGmtModified(gmtModifiedObj != null ? gmtModifiedObj.toString() : null);
                document.setBackgroundUrl(backgroundUrl);
                document.setUserId(userId);
                documents.add(document);
            } catch (Exception e) {
                log.warn("处理文章数据失败: {}", e.getMessage());
            }
        }

        articleRepository.saveAll(documents);

    }
    
    // 检查缓存状态
    private void checkCacheStatus() {
        log.info("检查缓存状态...");
        
        try {
            // 检查首页文章缓存
            log.info("第一次调用首页文章接口...");
            Page<ArticleExcerptDTO> homepageResult1 = articleService.getHomepageArticle(1, 10);
            log.info("第一次调用结果：获取到{}条记录", homepageResult1.getRecords().size());
            
            log.info("第二次调用首页文章接口（应该从缓存获取）...");
            Page<ArticleExcerptDTO> homepageResult2 = articleService.getHomepageArticle(1, 10);
            log.info("第二次调用结果：获取到{}条记录", homepageResult2.getRecords().size());
            
            // 检查热门文章缓存
            log.info("第一次调用热门文章接口...");
            List<ArticleExcerptDTO> popularResult1 = articleService.getPopularArticles(10);
            log.info("第一次调用结果：获取到{}条记录", popularResult1.size());
            
            log.info("第二次调用热门文章接口（应该从缓存获取）...");
            List<ArticleExcerptDTO> popularResult2 = articleService.getPopularArticles(10);
            log.info("第二次调用结果：获取到{}条记录", popularResult2.size());
            
            // 检查标签缓存
            log.info("第一次调用标签接口...");
            List<TagDTO> tagResult1 = tagService.selectAllTags();
            log.info("第一次调用结果：获取到{}个标签", tagResult1.size());
            
            log.info("第二次调用标签接口（应该从缓存获取）...");
            List<TagDTO> tagResult2 = tagService.selectAllTags();
            log.info("第二次调用结果：获取到{}个标签", tagResult2.size());
            
        } catch (Exception e) {
            log.error("检查缓存状态失败: {}", e.getMessage(), e);
        }
        
        // 直接检查 Redis 中的缓存数据
        checkRedisCacheData();
    }
    
    // 直接检查 Redis 中的缓存数据
    private void checkRedisCacheData() {
        log.info("直接检查 Redis 中的缓存数据...");
        
        try {
            // 测试 Redis 连接
            String testKey = "test:connection";
            redisTemplate.opsForValue().set(testKey, "test_value");
            String testValue = (String) redisTemplate.opsForValue().get(testKey);
            log.info("Redis 连接测试: {}", "test_value".equals(testValue) ? "成功" : "失败");
            redisTemplate.delete(testKey);
            
            // 检查所有以 hive:: 开头的键
            Set<String> keys = redisTemplate.keys("hive::*");
            log.info("Redis 中所有以 hive:: 开头的键数量: {}", keys != null ? keys.size() : 0);
            
            if (keys != null && !keys.isEmpty()) {
                log.info("Redis 中的键列表:");
                for (String key : keys) {
                    log.info("  - {}", key);
                }
            } else {
                log.warn("Redis 中没有找到任何以 hive:: 开头的键！");
            }
            
            // 检查所有键（用于调试）
            Set<String> allKeys = redisTemplate.keys("*");
            log.info("Redis 中所有键的数量: {}", allKeys != null ? allKeys.size() : 0);
            if (allKeys != null && !allKeys.isEmpty()) {
                log.info("Redis 中的所有键（前10个）:");
                int count = 0;
                for (String key : allKeys) {
                    if (count >= 10) break;
                    log.info("  - {}", key);
                    count++;
                }
            }
            
            // 检查特定的缓存键
            String homepageKey = "hive::articles::list::homepage::page:1::size:10";
            Object homepageValue = redisTemplate.opsForValue().get(homepageKey);
            log.info("首页文章缓存键 {} 的值: {}", homepageKey, homepageValue != null ? "存在" : "不存在");
            
        } catch (Exception e) {
            log.error("检查 Redis 缓存数据失败: {}", e.getMessage(), e);
        }
    }
}
