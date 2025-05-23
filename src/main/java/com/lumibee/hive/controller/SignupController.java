package com.lumibee.hive.controller;

import com.lumibee.hive.dto.SignupDTO;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class SignupController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 显示注册页面
    @GetMapping("/signup")
    public String signupPage(Model model) {
        if (!model.containsAttribute("signupDTO")) {
            model.addAttribute("signupDTO", new SignupDTO());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute("signupDTO") SignupDTO signupDTO,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        // 1. 基本表单校验结果 (来自 SignupDTO 的注解)
        if (bindingResult.hasErrors()) {
            return "signup"; // 返回注册页面，显示错误
        }

        // 2. 检查密码和确认密码是否一致
        if (!signupDTO.getPassword().equals(signupDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "密码和确认密码不一致");
        }

        // 3. 检查用户名是否已存在
        User existingUserByName = userService.selectByName(signupDTO.getUsername());
        if (existingUserByName != null) {
            bindingResult.rejectValue("username", "error.username", "该用户名已被注册");
        }

        // 4. 检查邮箱是否已存在
        User existingUserByEmail = userService.selectByEmail(signupDTO.getEmail());
        if (existingUserByEmail != null) {
            bindingResult.rejectValue("email", "error.email", "该邮箱已被注册");
        }

        // 如果存在任何自定义的校验错误（用户名或邮箱重复）
        if (bindingResult.hasErrors()) {
            return "signup"; // 返回注册页面，显示错误
        }

        // 4. 如果校验通过，创建新用户
        User newUser = new User();
        newUser.setName(signupDTO.getUsername());
        newUser.setEmail(signupDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(signupDTO.getPassword())); // 加密密码
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setGmtCreate(LocalDateTime.now());
        newUser.setGmtModified(newUser.getGmtCreate());
        newUser.setAvatarUrl(null);

        try {
            userService.insert(newUser);
        } catch (Exception e) {
            e.printStackTrace(); // 记录错误
            bindingResult.reject("error.global", "注册失败，请稍后再试或联系管理员。");
            return "signup";
        }

        // 5. 注册成功
        redirectAttributes.addFlashAttribute("signupSuccess", true);
        redirectAttributes.addFlashAttribute("signupMessage", "现在您可以使用新账户登录了。");
        return "redirect:/login";
    }

}