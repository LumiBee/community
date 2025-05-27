package com.lumibee.hive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortfolioController {

    @GetMapping("/portfolio")
    public String showPortfolio() {
        // This method will return the portfolio page
        return "portfolio"; // Assuming you have a Thymeleaf template named 'portfolio.html'
    }
}
