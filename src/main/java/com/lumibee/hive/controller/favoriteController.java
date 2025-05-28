package com.lumibee.hive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class favoriteController {

    @GetMapping("/favorites")
    public String showFavorites() {
        // This method will return the favorites page
        return "favorites"; // Assuming you have a Thymeleaf template named 'favorites.html'
    }

}
