package com.lumibee.hive.controller;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/settings")
public class SettingsController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String userSettingsPage(Model model, Authentication authentication, @RequestParam(name = "tab", required = false) String activeTab) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login"; // 用户未认证，重定向到登录
        }

        Object principal = authentication.getPrincipal();
        User currentUser = null;

        if (principal instanceof UserDetails) {
            String usernameOrEmail = ((UserDetails) principal).getUsername();
            currentUser = userService.selectByEmail(usernameOrEmail);
            if (currentUser == null) {
                currentUser = userService.selectByName(usernameOrEmail);
            }
        } else {
            OAuth2User oauth2User = (OAuth2User) principal;
            String githubId = oauth2User.getName(); // GitHub ID 或其他 OAuth ID
            currentUser = userService.selectByGithubId(githubId);

        }

        if (currentUser == null) {
            // 无法找到用户信息，可能需要记录错误并重定向
            System.err.println("Error: Could not load user details for settings page. Principal: " + principal.toString());
            return "redirect:/error/404"; // 或者一个更友好的错误提示页
        }

        model.addAttribute("user", currentUser); // <--- 这是关键，将用户对象添加到模型
        // model.addAttribute("userPreferences", ...); // 如果您有用户偏好设置

        if (activeTab != null) {
            model.addAttribute("activeTab", activeTab); // 用于JS激活特定tab
        }

        return "settings"; // 返回 settings.html 视图名
    }

    @PostMapping("/account")
    public String updatePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        User currentUser = null;

        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            currentUser = userService.selectByEmail(email);
        }else {
            OAuth2User oauth2User = (OAuth2User) principal;
            String name = oauth2User.getAttribute("login");
            if (name != null) {
                currentUser = userService.selectByName(name);
            }
        }


        //1.验证新密码和确认密码是否一致
        if (newPassword == null || newPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("passwordError", "新密码不能为空");
            return "redirect:/user/settings?tab=account&passwordError=new_empty";
        }

        if (!confirmNewPassword.equals(newPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "新密码和确认密码不一致");
            return "redirect:/user/settings?tab=account&passwordError=confirm_mismatch";
        }

        //2. 验证新密吗强度
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "新密码长度至少为6个字符");
            return "redirect:/user/settings?tab=account&passwordError=new_short";
        }

        //3. 加密新密码并更新到数据库
        String encryptedPassword = passwordEncoder.encode(newPassword);
        boolean success = userService.updatePassword(currentUser.getId(), encryptedPassword);
        if (success) {
            // 更新当前用户的密码
            currentUser.setPassword(encryptedPassword);
            session.setAttribute("user", currentUser);
            redirectAttributes.addFlashAttribute("passwordSuccess", "密码更新成功");
        } else {
            redirectAttributes.addFlashAttribute("passwordError", "密码更新失败，请稍后再试");
        }

        return "redirect:/user/settings?tab=account";
    }
}
