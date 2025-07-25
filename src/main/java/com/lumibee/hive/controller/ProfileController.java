package com.lumibee.hive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.ImgService;
import com.lumibee.hive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private ImgService imgService;
    @Autowired private ArticleService articleService;

    @PostMapping("/update-cover")
    @ResponseBody
    public Map<String, Object> updateUserCover(@RequestParam("coverImageFile") MultipartFile coverImageFile,
                                               @AuthenticationPrincipal Principal principal) {
        // 检查用户是否登录
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return Map.of("success", false, "message", "用户未登录或不存在");
        }

        // 验证背景图片
        if (coverImageFile.isEmpty() || coverImageFile.getSize() == 0) {
            return Map.of("success", false, "message", "背景图片不能为空");
        }

        try {
            String newImageUrl = imgService.uploadCover(currentUser.getId(), coverImageFile);
            // 更新用户信息
            currentUser.setBackgroundImgUrl(newImageUrl);

            return Map.of(
                "success", true,
                "message", "背景图片更新成功",
                "newImageUrl", newImageUrl
            );
        } catch (IOException e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/{name}")
    public String showUserProfile(@PathVariable("name") String name,
                                  @RequestParam(name = "page", defaultValue = "1") long pageNum,
                                  @RequestParam(name = "size", defaultValue = "6") long pageSize,
                                  Model model,
                                  @AuthenticationPrincipal Principal principal) {
        // 根据路径中的name查找用户
        User user = userService.selectByName(name);
        if (user == null) {
            // 如果用户不存在，可以跳转到404页面
            return "error/404";
        }

        // 判断正在查看的页面是否属于当前登录的用户
        boolean isOwner = false;
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (principal != null) {
            if (currentUser != null && currentUser.getId().equals(user.getId())) {
                isOwner = true;
            }
        }

        // 后续的业务逻辑和之前类似
        Integer articleCount = articleService.countArticlesByUserId(user.getId());
        Integer fans = userService.countFansByUserId(user.getId());
        Integer followers = userService.countFollowingByUserId(user.getId());
        Boolean isFollowed = userService.isFollowing(currentUser.getId(), user.getId());
        Page<ArticleExcerptDTO> articlePage = articleService.getProfilePageArticle(user.getId(), pageNum, pageSize);

        model.addAttribute("user", user);
        model.addAttribute("articleCount", articleCount);
        model.addAttribute("followersCount", fans);
        model.addAttribute("followingCount", followers);
        model.addAttribute("articles", articlePage);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isFollowed", isFollowed);
        System.out.println("User Profile: " + user.getName() + ", isOwner: " + isOwner);
        System.out.println("Article Count: " + articleCount);
        System.out.println("Followers Count: " + fans);
        System.out.println("Following Count: " + followers);
        System.out.println("Articles on Page: " + articlePage.getRecords().size());
        System.out.println("Is Followed: " + isFollowed);

        return "profile";
    }
}
