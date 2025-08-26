package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.UserService;

@RestController
@Tag(name = "文章管理", description = "文章相关的 API 接口")
public class ArticleController {

    @Autowired private ArticleService articleService;

    @Autowired private UserService userService;



    @PostMapping("/api/article/{articleId}/like")
    @Operation(summary = "切换文章点赞状态", description = "用户点赞或取消点赞文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<LikeResponse> toggleLike(
        @Parameter(description = "文章ID") @PathVariable("articleId") int articleId,
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
    @Operation(summary = "获取热门文章", description = "根据浏览量等指标获取热门文章列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<ArticleExcerptDTO>> getPopularArticles(
            @Parameter(description = "限制返回数量，默认6篇") @RequestParam(name = "limit", defaultValue = "6") int limit) {
        List<ArticleExcerptDTO> popularArticles = articleService.selectArticleSummaries(limit);
        return ResponseEntity.ok(popularArticles);
    }

    /**
     * 获取精选文章API
     */
    @GetMapping("/api/articles/featured")
    @ResponseBody
    public ResponseEntity<List<ArticleExcerptDTO>> getFeaturedArticles() {
        List<ArticleExcerptDTO> featuredArticles = articleService.selectFeaturedArticles();
        return ResponseEntity.ok(featuredArticles);
    }

    /**
     * 通过slug获取文章详情API
     */
    @GetMapping("/api/article/{slug}")
    @ResponseBody
    public ResponseEntity<ArticleDetailsDTO> getArticleBySlugAPI(@PathVariable("slug") String slug,
                                                                 @AuthenticationPrincipal Principal principal) {
        // 获取当前用户
        User user = userService.getCurrentUserFromPrincipal(principal);
        
        // 根据 slug 获取文章
        ArticleDetailsDTO article = articleService.getArticleBySlug(slug);

        if (article == null) {
            return ResponseEntity.notFound().build();
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
            // 未登录用户默认未关注和未点赞
            article.setFollowed(false);
            article.setLiked(false);
            article.setFavorited(false);
        }

        return ResponseEntity.ok(article);
    }

    /**
     * 获取相关文章API
     */
    @GetMapping("/api/article/{articleId}/related")
    @ResponseBody
    public ResponseEntity<List<ArticleDocument>> getRelatedArticles(@PathVariable("articleId") Integer articleId) {
        
        // 通过articleId获取文章详情，然后获取相关文章
        ArticleDetailsDTO article = articleService.getArticleByIdSimple(articleId);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<ArticleDocument> relatedArticles = articleService.selectRelatedArticles(article, 6);
        return ResponseEntity.ok(relatedArticles);
    }
}
