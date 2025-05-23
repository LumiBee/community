package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    @Override
    @Transactional(readOnly = true)
    public Page<Article> getHomepageArticle(long pageNum, long pageSize) {
        Page<Article> articlePageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getGmtCreate);

        Page<Article> articlePage = articleMapper.selectPage(articlePageRequest, queryWrapper);
        List<Article> articleList = new ArrayList<>();
        if (articlePage.getRecords() != null && !articlePage.getRecords().isEmpty()) {
            for (Article article : articlePage.getRecords()) {
                Article articleDTO = new Article();
                articleDTO.setArticleId(article.getArticleId());
                articleDTO.setUserId(article.getUserId());
                articleDTO.setUserName(userService.selectById(article.getUserId()).getName());
                articleDTO.setTitle(article.getTitle());
                articleDTO.setGmtCreate(article.getGmtCreate());
                articleDTO.setGmtModified(article.getGmtModified());
                articleDTO.setAvatarUrl(userService.selectById(article.getUserId()).getAvatarUrl());
                articleDTO.setExcerpt(article.getExcerpt());
                articleDTO.setLikes(article.getLikes());
                articleDTO.setViewCount(article.getViewCount());
                articleDTO.setTags(article.getTags());
                articleDTO.setSlug(article.getSlug());
                articleDTO.setPortfolioName(article.getPortfolioName());
                articleList.add(articleDTO);
            }
        }

        Page<Article> articleDTOPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        articleDTOPage.setRecords(articleList);
        articleDTOPage.setPages(articlePage.getPages());

        return articleDTOPage;
    }

    public String createUniqueSlug(String title) {
        String baseSlug = SlugGenerator.generateSlug(title);
        if (baseSlug.isEmpty()) {
            baseSlug = "article-" + System.currentTimeMillis();
        }

        String potentialSlug = baseSlug;
        int count = 0;
        while (articleMapper.selectBySlug(potentialSlug)) {
            potentialSlug = potentialSlug + "-" + count++;
        }

        return potentialSlug;
    }

    @Override
    public Article publishArticle(Article article, List<String> tagsName) {

        articleMapper.insert(article);
        if (tagsName != null && !tagsName.isEmpty()) {
            Set<Tag> tags = tagService.selectOrCreateTags(tagsName);
            for (Tag tag : tags) {
                articleMapper.insertArticleTag(article.getArticleId(), tag.getTagId());
            }
        }

        return article;
    }

    @Override
    public Article getArticleBySlug(String slug) {
        return articleMapper.findBySlug(slug);
    }

}
