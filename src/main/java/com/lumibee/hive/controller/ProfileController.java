package com.lumibee.hive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.ImgService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private ImgService imgService;
    @Autowired private ArticleService articleService;

    /**
     * 更新用户封面图片API
     */
    @PostMapping("/update-cover")
    public ResponseEntity<Map<String, Object>> updateUserCover(@RequestParam("coverImageFile") MultipartFile coverImageFile,
                                                              @AuthenticationPrincipal Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        // 检查用户是否登录
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "用户未登录或不存在");
            return ResponseEntity.status(401).body(response);
        }

        // 验证背景图片
        if (coverImageFile.isEmpty() || coverImageFile.getSize() == 0) {
            response.put("success", false);
            response.put("message", "背景图片不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String newImageUrl = imgService.uploadCover(currentUser.getId(), coverImageFile);
            // 更新用户信息
            currentUser.setBackgroundImgUrl(newImageUrl);

            response.put("success", true);
            response.put("message", "背景图片更新成功");
            response.put("newImageUrl", newImageUrl);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 重定向用户资料页到Vue SPA
     */
    @GetMapping("/profile/{name}")
    public ResponseEntity<Void> redirectToUserProfileSPA(@PathVariable("name") String name) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/profile/" + name)
                .build();
    }

    /**
     * 获取用户资料数据API
     */
    @GetMapping("/api/profile/{name}")
    public ResponseEntity<Map<String, Object>> getUserProfileData(@PathVariable("name") String name,
                                                                  @RequestParam(name = "page", defaultValue = "1") long pageNum,
                                                                  @RequestParam(name = "size", defaultValue = "6") long pageSize,
                                                                  @AuthenticationPrincipal Principal principal) {
        System.out.println("获取用户资料: " + name + ", 页码: " + pageNum + ", 大小: " + pageSize);
        
        try {
            // 根据路径中的name查找用户
            User user = userService.selectByName(name);
            if (user == null) {
                System.out.println("用户不存在: " + name);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("找到用户: " + user.getId() + ", " + user.getName());

            // 判断正在查看的页面是否属于当前登录的用户
            boolean isOwner = false;
            User currentUser = userService.getCurrentUserFromPrincipal(principal);
            if (principal != null && currentUser != null && currentUser.getId().equals(user.getId())) {
                isOwner = true;
                System.out.println("当前用户是页面所有者");
            }

            // 获取用户统计数据
            Integer articleCount = articleService.countArticlesByUserId(user.getId());
            Integer fans = userService.countFansByUserId(user.getId());
            Integer followers = userService.countFollowingByUserId(user.getId());
            Boolean isFollowed = currentUser != null ? userService.isFollowing(currentUser.getId(), user.getId()) : false;
            Page<ArticleExcerptDTO> articlePage = articleService.getProfilePageArticle(user.getId(), pageNum, pageSize);

            System.out.println("用户统计: 文章=" + articleCount + ", 粉丝=" + fans + ", 关注=" + followers);
            System.out.println("文章分页: 总数=" + articlePage.getTotal() + ", 页数=" + articlePage.getPages());

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("articleCount", articleCount);
            response.put("followersCount", fans);
            response.put("followingCount", followers);
            response.put("articles", articlePage);
            response.put("isOwner", isOwner);
            response.put("isFollowed", isFollowed);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取用户资料出错: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取用户资料失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
