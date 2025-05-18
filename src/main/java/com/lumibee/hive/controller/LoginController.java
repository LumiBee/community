package com.lumibee.hive.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@ModelAttribute("signupSuccess")String signupSuccess,
                        @ModelAttribute("signupMessage")String signupMessage,
                        Model model) {
        if ("true".equals(signupSuccess) && signupMessage != null) {
            model.addAttribute("showSignupSuccessPopup", true);
            model.addAttribute("popupMessage", signupMessage);
        }

        return "login";
    }

    @PostMapping("/login")
    public String loginSuccess() {
        return "redirect:/";
    }

    @PostMapping("/api/user/dismiss-password-prompt")
    @ResponseBody
    public ResponseEntity<String> dismissPasswordPrompt(HttpSession session) {
        if (session != null) {
            session.removeAttribute("showPasswordSetupPrompt");
            System.out.println("Session attribute 'showPasswordSetupPrompt' removed by user action.");
        }
        return ResponseEntity.ok("Prompt dismissed");
    }

}
