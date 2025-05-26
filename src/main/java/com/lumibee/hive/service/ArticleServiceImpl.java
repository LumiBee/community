package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.mapper.ArticleLikesMapper;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private ArticleLikesMapper articleLikesMapper;

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

        article.setGmtCreate(LocalDateTime.now());
        article.setGmtModified(LocalDateTime.now());
        article.setStatus(Article.ArticleStatus.published);
        articleMapper.insert(article);
        if (tagsName != null && !tagsName.isEmpty()) {
            Set<Tag> tags = tagService.selectOrCreateTags(tagsName);
            tags.forEach(tag -> {
                System.out.println(tag.getName());
            });
        }

        return article;
    }

    @Override
    public Article getArticleBySlug(String slug) {
        Article article = articleMapper.findBySlug(slug);
        article.setTags(tagService.selectTagsByArticleId(article.getArticleId()));
        return article;
    }

    @Override
    @Transactional
    public LikeResponse toggleLike(long userId, int articleId) {
        //获取当前文章的点赞状态
        boolean isCurrentlyLiked = articleLikesMapper.toggleLike(userId, articleId) == null;
        //用来储存用户的点赞状态
        boolean newLikedStatus;

        if (isCurrentlyLiked) {
            //如果当前用户没有点赞，则插入点赞记录并更新文章的点赞状态
            articleLikesMapper.insertLike(userId, articleId);
            articleMapper.updateArticleLikes(articleId, 1);
            newLikedStatus = true;
        }else {
            //如果当前用户已经点赞，则删除点赞记录并更新文章的点赞状态
            articleLikesMapper.deleteLike(userId, articleId);
            articleMapper.updateArticleLikes(articleId, -1);
            newLikedStatus = false;
        }

        //获取当前文章的点赞数量
        Integer likesCount = articleMapper.countLikes(articleId);

        return new LikeResponse(true, newLikedStatus, likesCount);
    }

    @Override
    public boolean isUserLiked(long userId, int articleId) {
        return articleLikesMapper.toggleLike(userId, articleId) != null;
    }

    @Override
    public void incrementViewCount(Integer articleId) {
        articleMapper.incrementViewCount(articleId);
    }

    @Override
    public List<Article> getTopArticles() {
        return articleMapper.getTopArticles();
    }

}
