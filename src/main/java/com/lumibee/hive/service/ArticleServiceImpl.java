package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.entity.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserServiceImpl userServiceImpl;

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
                articleDTO.setTitle(article.getTitle());
                articleDTO.setGmtCreate(article.getGmtCreate());
                articleDTO.setGmtModified(article.getGmtModified());
                articleDTO.setCoverImageUrl(userServiceImpl.selectById(article.getUserId()).getAvatarUrl());
                articleList.add(articleDTO);
            }
        }

        Page<Article> articleDTOPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        articleDTOPage.setRecords(articleList);
        articleDTOPage.setPages(articlePage.getPages());

        return articleDTOPage;
    }


}
