package com.lumibee.hive.controller;

import com.lumibee.hive.model.User;
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
}
