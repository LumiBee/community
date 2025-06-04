package com.lumibee.hive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.PortfolioDTO;
import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Portfolio;
import com.lumibee.hive.model.Tag;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.TagService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class IndexController {

    @Autowired private ArticleService articleService;
    @Autowired private TagService tagService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private UserService userService;

    @GetMapping("/")
    public String home(@RequestParam(name = "page", defaultValue = "1") long pageNum,
                       @RequestParam(name = "size", defaultValue = "6") long pageSize,
                       Model model) {
        int limit = 6;

        Page<ArticleExcerptDTO> articlePage = articleService.getHomepageArticle(pageNum, pageSize);
        List<ArticleExcerptDTO> topArticles = articleService.selectArticleSummaries(limit);
        List<TagDTO> allTags = tagService.selectAllTags();

        model.addAttribute("articles", articlePage);
        model.addAttribute("popularArticles", topArticles);
        model.addAttribute("tags", allTags);

        return "index";
    }


    @GetMapping("/publish")
    public String publish() {
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
    public String showProfile(Model model,
                              @AuthenticationPrincipal Principal principal) {
        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return "redirect:/login";
        }

        Integer articleCount = articleService.countArticlesByUserId(user.getId());
        Integer fans = userService.countFansByUserId(user.getId());
        Integer following = userService.countFollowingByUserId(user.getId());
        List<ArticleExcerptDTO> articleExcerptDTO = articleService.getArticlesByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("articleCount", articleCount);
        model.addAttribute("followersCount", fans);
        model.addAttribute("followingCount", following);
        model.addAttribute("articles", articleExcerptDTO);

        return "profile";
    }

    @GetMapping("/messages")
    public String showMessages() {
        return "messages";
    }

}
