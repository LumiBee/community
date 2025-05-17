package com.lumibee.hive.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
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
