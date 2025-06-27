package com.lumibee.hive.controller;

import java.security.Principal;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.UserService;

@Controller
public class ArticleController {

    @Autowired private ArticleService articleService;

    @Autowired private UserService userService;

    @GetMapping("/article/{slug}")
    public String viewArticle(@PathVariable("slug") String slug,
                              @AuthenticationPrincipal Principal principal,
                              Model model) {
        // 获取当前用户
        User user = userService.getCurrentUserFromPrincipal(principal);
        // 根据 slug 获取文章
        ArticleDetailsDTO article = articleService.getArticleBySlug(slug);

        if (article == null) {
            // 如果文章不存在，返回 404 页面
            return "error/404";
        }

        if (user != null) {
            System.out.println("当前用户ID: " + user.getId() + ", 文章作者ID: " + article.getUserId());
            
            // 检查用户是否点赞
            boolean isLiked = articleService.isUserLiked(user.getId(), article.getArticleId());
            article.setLiked(isLiked);
            
            // 判断当前用户是否关注了作者
            boolean followedByCurrentUser = userService.isFollowing(user.getId(), article.getUserId());
            article.setFollowedByCurrentUser(followedByCurrentUser);
            
            System.out.println("是否关注作者: " + followedByCurrentUser);
        } else {
            System.out.println("当前用户未登录，默认未关注作者");
            // 未登录用户默认未关注和未点赞
            article.setFollowedByCurrentUser(false);
            article.setLiked(false);
        }

        String markdownContent = article.getContent();
        String renderedHtmlContent = "";
        // 解析 Markdown 内容
        if (markdownContent != null) {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdownContent);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            renderedHtmlContent = renderer.render(document);
        }

        // 增加文章浏览量
        articleService.incrementViewCount(article.getArticleId());

        model.addAttribute("article", article);
        model.addAttribute("renderedHtmlContent", renderedHtmlContent);
        System.out.println("文章对象中的关注状态: " + article.isFollowedByCurrentUser());

        return "article";
    }

    @PostMapping("/api/article/{articleId}/like")
    public ResponseEntity<LikeResponse> toggleLike(@PathVariable("articleId") int articleId,
                                                   @AuthenticationPrincipal Principal principal) {

        // 获取当前用户
        User user = userService.getCurrentUserFromPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 调用服务层方法切换点赞状态
        LikeResponse response = articleService.toggleLike(user.getId(), articleId);

        return ResponseEntity.ok(response);
    }


}
