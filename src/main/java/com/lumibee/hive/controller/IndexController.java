package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.List;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.model.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.TagService;
import com.lumibee.hive.service.UserService;

@Controller
public class IndexController {

    @Autowired private ArticleService articleService;
    @Autowired private TagService tagService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private UserService userService;

    @GetMapping("/")
    public String home(@RequestParam(name = "page", defaultValue = "1") long pageNum,
                       @RequestParam(name = "size", defaultValue = "8") long pageSize,
                       Model model) {
        int limit = 6;


        Page<ArticleExcerptDTO> articlePage = articleService.getHomepageArticle(pageNum, pageSize);
        List<ArticleExcerptDTO> popularArticles = articleService.selectArticleSummaries(limit);
        List<ArticleExcerptDTO> featuredArticles = articleService.selectFeaturedArticles("Markdown基本语法");
        List<TagDTO> allTags = tagService.selectAllTags();

        model.addAttribute("articles", articlePage);
        model.addAttribute("popularArticles", popularArticles);
        model.addAttribute("tags", allTags);
        model.addAttribute("featuredArticles", featuredArticles);

        return "index";
    }


    @GetMapping("/publish")
    public String publish(@RequestParam(name = "draftId", required = false) Integer draftId,
                          Model model) {
        if (draftId != null) {
            ArticleDetailsDTO draft = articleService.selectDraftById(draftId);
            // System.out.println(draft.getContent());
            if (draft != null) {
                model.addAttribute("article", draft);
            }
        }
        return "publish";
    }

    @GetMapping("/tags")
    public String showAllTags(Model model) {
        List<TagDTO> allTags = tagService.selectAllTags();
        model.addAttribute("allTags", allTags);
        return "tags";
    }

    @GetMapping("/portfolio")
    public String showPortfolio(Model model) {
        List<PortfolioDetailsDTO> allPortfolios = portfolioService.selectAllPortfolios();
        model.addAttribute("allPortfolios", allPortfolios);
        return "portfolio";
    }

    @GetMapping("/profile")
    public String showProfile(@RequestParam(name = "page", defaultValue = "1") long pageNum,
                              @RequestParam(name = "size", defaultValue = "6") long pageSize,
                              Model model,
                              @AuthenticationPrincipal Principal principal) {
        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return "redirect:/login";
        }
        // 判断是否为当前用户的个人资料页面
        boolean isOwner = true;

        Integer articleCount = articleService.countArticlesByUserId(user.getId());
        Integer fans = userService.countFansByUserId(user.getId());
        Integer following = userService.countFollowingByUserId(user.getId());
        Page<ArticleExcerptDTO> articlePage = articleService.getProfilePageArticle(user.getId(), pageNum, pageSize);

        model.addAttribute("user", user);
        model.addAttribute("articleCount", articleCount);
        model.addAttribute("followersCount", fans);
        model.addAttribute("followingCount", following);
        model.addAttribute("articles", articlePage);

        model.addAttribute("isOwner", isOwner);

        return "profile";
    }

    @GetMapping("/messages")
    public String showMessages() {
        return "messages";
    }

}
