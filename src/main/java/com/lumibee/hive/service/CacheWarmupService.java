package com.lumibee.hive.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
public class CacheWarmupService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private TagService tagService;

    @Autowired
    private PortfolioService portfolioService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupCaches() {
        log.info("开始预热缓存...");

        // 只预热核心数据
        warmupCoreData();

        // 延迟预热其他数据
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(30000); // 30秒后预热其他数据
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
        warmupCoreData();
        warmupOtherData();
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
            var tags = tagService.selectAllTags();
            for (var tag : tags) {
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
            var portfolios = portfolioService.selectAllPortfolios();
            for (var portfolio : portfolios) {
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

}
