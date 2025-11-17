package com.lumibee.hive.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lumibee.hive.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.ImgService;
import com.lumibee.hive.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@Tag(name = "个人中心管理", description = "个人中心相关的 API 接口")
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private ImgService imgService;
    @Autowired private ArticleService articleService;

    /**
     * 更新用户封面图片API
     */
    @PostMapping("/update-cover")
    @Operation(summary = "更新用户封面图片", description = "上传并更新用户的背景封面图片")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户未认证"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> updateUserCover(
            @Parameter(description = "封面图片文件") @RequestParam("coverImageFile") MultipartFile coverImageFile,
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
     * 获取用户资料数据API
     */
    @GetMapping("/profile/{name}")
    @Operation(summary = "获取用户资料数据", description = "根据用户名获取用户的详细资料信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getUserProfileData(
            @Parameter(description = "用户名") @PathVariable("name") String name,
            @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "1") long pageNum,
            @Parameter(description = "每页大小") @RequestParam(name = "size", defaultValue = "6") long pageSize,
            @AuthenticationPrincipal Principal principal) {
        
        try {
            // 根据路径中的name查找用户
            User user = userService.selectByName(name);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            

            // 判断正在查看的页面是否属于当前登录的用户
            boolean isOwner = false;
            User currentUser = userService.getCurrentUserFromPrincipal(principal);
            if (principal != null && currentUser != null && currentUser.getId().equals(user.getId())) {
                isOwner = true;
            }

            // 获取用户统计数据
            Integer articleCount = articleService.countArticlesByUserId(user.getId());
            Integer fans = userService.countFansByUserId(user.getId());
            Integer followers = userService.countFollowingByUserId(user.getId());
            Boolean isFollowed = currentUser != null ? userService.isFollowing(currentUser.getId(), user.getId()) : false;
            Page<ArticleExcerptDTO> articlePage = articleService.getProfilePageArticle(user.getId(), pageNum, pageSize, false);


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

    @GetMapping("/profile/{name}/fans")
    @Operation(summary = "获取用户的粉丝列表", description = "根据用户名获取用户的粉丝列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getUserFollowers(
            @Parameter(description = "用户名") @PathVariable("name") String name,
            @Parameter(description = "页码") @RequestParam(name = "page", defaultValue = "1") long pageNum,
            @Parameter(description = "每页大小") @RequestParam(name = "size", defaultValue = "6") long pageSize,
            @AuthenticationPrincipal Principal principal) {

        try {
            // 根据路径中的name查找用户
            User user = userService.selectByName(name);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // 判断正在查看的页面是否属于当前登录的用户
            boolean isOwner = false;
            User currentUser = userService.getCurrentUserFromPrincipal(principal);
            if (principal != null && currentUser != null && currentUser.getId().equals(user.getId())) {
                isOwner = true;
            }

            List<UserDTO> fans = userService.findFans(user.getId());
            HashMap<String, Object> response = new HashMap<>();

            response.put("user", user);
            response.put("followers", fans);
            response.put("isOwner", isOwner);

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
