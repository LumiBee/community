package com.lumibee.hive.controller;

import com.lumibee.hive.model.Tag;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private ArticleService articleService;

    @GetMapping("/tags")
    public String showAllTags(Model model) {
        List<Tag> allTags = tagService.selectAllTags();

        model.addAttribute("allTags", allTags);

        return "tags";
    }
}
