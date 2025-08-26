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
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataSyncController implements CommandLineRunner {

    @Autowired private ArticleRepository articleRepository;
    @Autowired private ArticleService articleService;
    @Autowired private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================start=================");
        List<ArticleDetailsDTO> articles = articleService.selectAll();
        if (articles == null || articles.isEmpty()) {
            System.out.println("No articles found to sync with Elasticsearch.");
            return ;
        }

        // 批量查询用户信息，避免在循环中逐个查询
        List<Long> userIds = articles.stream()
                .map(ArticleDetailsDTO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, User> userMap = userService.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<ArticleDocument> documents = new ArrayList<>();
        for (ArticleDetailsDTO article : articles) {
            User user = userMap.get(article.getUserId());

            ArticleDocument document = new ArticleDocument();
            document.setId(article.getArticleId());
            document.setTitle(article.getTitle());
            document.setContent(article.getContent());
            document.setSlug(article.getSlug());
            document.setLikes(article.getLikes());
            document.setViewCount(article.getViewCount());
            document.setUserName(user != null ? user.getName() : "");
            document.setAvatarUrl(user != null ? user.getAvatarUrl() : "");
            documents.add(document);
        }

        articleRepository.saveAll(documents);
        System.out.println("Data sync completed. " + documents.size() + " articles indexed.");
        System.out.println("=================over=================");

    }
}
