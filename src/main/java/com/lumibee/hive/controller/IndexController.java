package com.lumibee.hive.controller;

import com.lumibee.hive.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @GetMapping("/")
    public String home(@RequestParam(name = "page", defaultValue = "1") long pageNum,
                       @RequestParam(name = "size", defaultValue = "10") long pageSize) {
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/article")
    public String article() {
        return "article";
    }


}
