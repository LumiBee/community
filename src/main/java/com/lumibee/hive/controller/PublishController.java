package com.lumibee.hive.controller;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.TagService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/article")
public class PublishController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;


    @PostMapping("/publish")
    public ResponseEntity<ArticleDetailsDTO> publishArticle(@AuthenticationPrincipal Principal principal,
                                                            @RequestBody ArticlePublishRequestDTO requestDTO) {

        Long userId = userService.getCurrentUserFromPrincipal(principal).getId();

        ArticleDetailsDTO newArticle = articleService.publishArticle(requestDTO, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newArticle);
    }
}
