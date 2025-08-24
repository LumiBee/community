package com.lumibee.hive.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

import com.lumibee.hive.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ImgService;
import com.lumibee.hive.service.CustomUserServiceImpl;
import com.lumibee.hive.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/user/settings")
public class SettingsController {

    @Autowired private UserService userService;
    @Autowired private ImgService imgService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private FileStorageService fileStorageService;

    @GetMapping
    public String userSettingsPage(Model model,
                                   @AuthenticationPrincipal Principal principal,
                                   @RequestParam(name = "tab", required = false) String activeTab) {
        User currentUser = userService.getCurrentUserFromPrincipal(principal);

        if (currentUser == null) {
            // 无法找到用户信息，可能需要记录错误并重定向
            System.err.println("Error: Could not load user details for settings page. Principal: " + principal.toString());
            return "redirect:/error/404";
        }

        model.addAttribute("user", currentUser);

        if (activeTab != null) {
            model.addAttribute("activeTab", activeTab);
        }

        return "settings";
    }

    @PostMapping("/profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestParam("username") String username,
                                           @RequestParam("bio") String bio,
                                           @RequestParam("email") String email,
                                           @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                           @AuthenticationPrincipal Principal principal) {
        
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            return new ResponseEntity<>("用户信息获取失败", HttpStatus.BAD_REQUEST);
        }
        
        // 更新基本信息
        currentUser.setName(username);
        currentUser.setBio(bio);
        currentUser.setEmail(email);
        currentUser.setGmtModified(LocalDateTime.now());
        
        // 处理头像上传
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarUrl = imgService.uploadAvatar(currentUser.getId(), avatarFile);
                currentUser.setAvatarUrl(avatarUrl);
            } catch (IOException e) {
                System.err.println("Avatar upload error: " + e.getMessage());
                return new ResponseEntity<>("头像上传失败，请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        
        userService.updateById(currentUser);
        userService.refreshUserPrincipal(currentUser);
        
        return new ResponseEntity<>("用户信息更新成功", HttpStatus.OK);
    }

    @PostMapping("/account")
    public String updatePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 @AuthenticationPrincipal Principal principal,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = userService.getCurrentUserFromPrincipal(principal);

        //1.验证新密码和确认密码是否一致
        if (newPassword == null || newPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("passwordError", "新密码不能为空");
            return "redirect:/settings?tab=account&passwordError=new_empty";
        }

        if (!confirmNewPassword.equals(newPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "新密码和确认密码不一致");
            return "redirect:/settings?tab=account&passwordError=confirm_mismatch";
        }

        //2. 验证新密吗强度
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "新密码长度至少为6个字符");
            return "redirect:/settings?tab=account&passwordError=new_short";
        }

        //3. 加密新密码并更新到数据库
        String encryptedPassword = passwordEncoder.encode(newPassword);
        boolean success = userService.updatePassword(currentUser.getId(), encryptedPassword);
        if (success) {
            // 更新当前用户的密码
            HttpSession session = ((org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                    .getRequest().getSession();

            redirectAttributes.addFlashAttribute("passwordSuccess", "密码更新成功");
            session.removeAttribute("showPasswordSetupPrompt");
        } else {
            redirectAttributes.addFlashAttribute("passwordError", "密码更新失败，请稍后再试");
        }

        return "redirect:/settings?tab=account";
    }
    
    @PostMapping("/preferences")
    public String updatePreferences(@RequestParam(value = "emailNotifications", required = false) boolean emailNotifications,
                                   @RequestParam("interfaceTheme") String interfaceTheme,
                                   @AuthenticationPrincipal Principal principal,
                                   RedirectAttributes redirectAttributes) {
        
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("preferencesError", "用户信息获取失败");
            return "redirect:/settings?tab=preferences";
        }
        
        // 这里应该保存用户偏好设置
        // 如果没有UserPreferences实体，可以考虑创建一个
        
        redirectAttributes.addFlashAttribute("preferencesSuccess", "偏好设置更新成功");
        return "redirect:/settings?tab=preferences";
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "images", required = false) String subDirectory) {
        try {
            String filePath = fileStorageService.storeFile(file, subDirectory);
            String fileUrl = fileStorageService.getBaseUrl() + filePath;

            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
}
