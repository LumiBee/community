package com.lumibee.hive.controller;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleRepository;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSyncController implements CommandLineRunner {

    @Autowired private ArticleRepository articleRepository;
    @Autowired private ArticleService articleService;
    @Autowired private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================start=================");
        List<ArticleDetailsDTO> articles = articleService.selectAll();
        if (articles == null && articles.isEmpty()) {
            System.out.println("No articles found to sync with Elasticsearch.");
            return ;
        }

        List<ArticleDocument> documents = new ArrayList<>();
        for (ArticleDetailsDTO article : articles) {
            User user = userService.selectById(article.getUserId());

            ArticleDocument document = new ArticleDocument();
            document.setId(article.getArticleId());
            document.setTitle(article.getTitle());
            document.setContent(article.getContent());
            document.setSlug(article.getSlug());
            document.setLikes(article.getLikes());
            document.setViewCount(article.getViewCount());
            document.setUserName(user.getName());
            document.setAvatarUrl(user.getAvatarUrl());
            documents.add(document);
        }

        articleRepository.saveAll(documents);
        System.out.println("Data sync completed. " + documents.size() + " articles indexed.");
        System.out.println("=================over=================");

    }
}
