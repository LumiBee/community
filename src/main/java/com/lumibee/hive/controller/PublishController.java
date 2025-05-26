package com.lumibee.hive.controller;

import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.TagService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class PublishController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;


    @GetMapping("/publish")
    public String publish() {
        return "publish";
    }

    @PostMapping("/article/publish")
    public String publishArticle(@RequestParam("title") String title,
                                 @RequestParam("content")String content,
                                 @RequestParam("tags")String tags,
                                 @RequestParam("summary")String excerpt,
                                 @RequestParam(value = "portfolio", required = false)String portfolioName,
                                 @AuthenticationPrincipal Principal principal,
                                 RedirectAttributes redirectAttributes) {

        // 1. 参数校验
        User user = userService.getCurrentUserFromPrincipal(principal);

        if (title == null || title.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "标题不能为空");
            return "redirect:/publish";
        }

        if (content == null || content.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "内容不能为空");
            return "redirect:/publish";
        }

        if (tags == null || tags.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "标签不能为空");
            return "redirect:/publish";
        }

        // 2. 准备文章数据
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setExcerpt(excerpt);
        article.setSlug(articleService.createUniqueSlug(title));
        article.setUserId(user.getId());

        // 3. 处理标签
        List<String> tagsName = null;
        if (!tags.trim().isEmpty()) {
            tagsName = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .toList();
        }

        // 4. 调用 Service 层保存文章
        Article savedArticle = articleService.publishArticle(article, tagsName, portfolioName);
        redirectAttributes.addFlashAttribute("successMessage", "文章发布成功");
        return "redirect:/article/" + savedArticle.getSlug();
    }
}
