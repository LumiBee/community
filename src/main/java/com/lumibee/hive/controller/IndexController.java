package com.lumibee.hive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.service.ArticleService;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    @Autowired
    private ArticleService articleService;

    @GetMapping("/")
    public String home(@RequestParam(name = "page", defaultValue = "1") long pageNum,
                       @RequestParam(name = "size", defaultValue = "6") long pageSize,
                       Model model) {
        Page<Article> articlePage = articleService.getHomepageArticle(pageNum, pageSize);
        articlePage.getRecords().forEach(article -> {
            System.out.println(article.getSlug());
        });

        model.addAttribute("articles", articlePage);

        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/article/{slug}")
    public String viewArticle(@PathVariable("slug") String slug, Model model) {
        Article article = articleService.getArticleBySlug(slug);

        if (article == null) {
            return "error/404";
        }

        String markdownContent = article.getContent();
        String renderedHtmlContent = "";
        if (markdownContent != null) {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdownContent);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            renderedHtmlContent = renderer.render(document);
        }
        //articleService.incrementViewCount(article.getArticleId());
        model.addAttribute("article", article);
        model.addAttribute("renderedHtmlContent", renderedHtmlContent);

        return "article";
    }


}
