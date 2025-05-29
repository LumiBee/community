package com.lumibee.hive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Portfolio;
import com.lumibee.hive.model.Tag;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private TagService tagService;

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/")
    public String home(@RequestParam(name = "page", defaultValue = "1") long pageNum,
                       @RequestParam(name = "size", defaultValue = "6") long pageSize,
                       Model model) {
        int limit = 6;

        Page<Article> articlePage = articleService.getHomepageArticle(pageNum, pageSize);
        List<Article> topArticles = articleService.getArticlesLimit(limit);
        List<Tag> allTags = tagService.selectAllTags();

        model.addAttribute("articles", articlePage);
        model.addAttribute("popularArticles", topArticles);
        model.addAttribute("tags", allTags);

        return "index";
    }

    @GetMapping("/tags")
    public String showAllTags(Model model) {
        List<Tag> allTags = tagService.selectAllTags();
        model.addAttribute("allTags", allTags);
        return "tags";
    }

    @GetMapping("/portfolio")
    public String showPortfolio(Model model) {
        List<Portfolio> allPortfolios = portfolioService.selectAllPortfolios();
        System.out.println("All Portfolios: " + allPortfolios);
        model.addAttribute("allPortfolios", allPortfolios);

        return "portfolio";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }


}
