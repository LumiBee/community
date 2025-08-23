package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.model.ArticleDocument;
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

        // 获取文章的总收藏数
        int favoriteCount = articleService.getFavoriteCount(article.getArticleId());
        article.setFavoriteCount(favoriteCount);

        if (user != null) {
            // 检查用户是否点赞
            boolean isLiked = articleService.isUserLiked(user.getId(), article.getArticleId());
            article.setLiked(isLiked);
            
            // 判断当前用户是否关注了作者
            boolean followedByCurrentUser = userService.isFollowing(user.getId(), article.getUserId());
            article.setFollowed(followedByCurrentUser);

            // 判断当前用户是否收藏了文章
            boolean favoritedByCurrentUser = userService.isFavoritedByCurrentUser(user.getId(), article.getArticleId());
            article.setFavorited(favoritedByCurrentUser);
        } else {
            System.out.println("当前用户未登录，默认未关注作者");
            // 未登录用户默认未关注和未点赞
            article.setFollowed(false);
            article.setLiked(false);
            article.setFavorited(false);
        }

        List<ArticleDocument> relatedArticles = articleService.selectRelatedArticles(article,6);
        model.addAttribute("relatedArticles", relatedArticles);

        String markdownContent = article.getContent();
        String renderedHtmlContent = "";
        // 解析 Markdown 内容
        if (markdownContent != null) {
            List<Extension> extensions = Arrays.asList(TablesExtension.create());
            Parser parser = Parser.builder()
                    .extensions(extensions)
                    .build();
            Node document = parser.parse(markdownContent);
            HtmlRenderer renderer = HtmlRenderer.builder()
                    .extensions(extensions)
                    .build();
            renderedHtmlContent = renderer.render(document);
        }

        model.addAttribute("article", article);
        model.addAttribute("renderedHtmlContent", renderedHtmlContent);

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

    /**
     * 获取热门文章API
     */
    @GetMapping("/api/articles/popular")
    @ResponseBody
    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
    public ResponseEntity<List<ArticleExcerptDTO>> getPopularArticles(
            @RequestParam(name = "limit", defaultValue = "6") int limit) {
        List<ArticleExcerptDTO> popularArticles = articleService.selectArticleSummaries(limit);
        return ResponseEntity.ok(popularArticles);
    }

    /**
     * 获取精选文章API
     */
    @GetMapping("/api/articles/featured")
    @ResponseBody
    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
    public ResponseEntity<List<ArticleExcerptDTO>> getFeaturedArticles() {
        List<ArticleExcerptDTO> featuredArticles = articleService.selectFeaturedArticles();
        return ResponseEntity.ok(featuredArticles);
    }
}
