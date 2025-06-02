package com.lumibee.hive.controller;

import com.lumibee.hive.model.User;
import com.lumibee.hive.service.CustomUserServiceImpl;
import com.lumibee.hive.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import java.security.Principal;

@Controller
@RequestMapping("/user/settings")
public class SettingsController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private CustomUserServiceImpl userDetailsService;

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

    @PostMapping("/account")
    public String updatePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 @AuthenticationPrincipal Principal principal,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = userService.getCurrentUserFromPrincipal(principal);

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
            HttpSession session = ((org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                    .getRequest().getSession();

            redirectAttributes.addFlashAttribute("passwordSuccess", "密码更新成功");
            session.removeAttribute("showPasswordSetupPrompt");
        } else {
            redirectAttributes.addFlashAttribute("passwordError", "密码更新失败，请稍后再试");
        }

        return "redirect:/user/settings?tab=account";
    }
}
