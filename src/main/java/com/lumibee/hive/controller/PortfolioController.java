package com.lumibee.hive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;

@Controller
public class PortfolioController {

    @Autowired private PortfolioMapper portfolioMapper;

    @Autowired private PortfolioService portfolioService;

    @Autowired private ArticleService articleService;

    @GetMapping("/portfolio/{id}")
    public String getPortfolio(@PathVariable("id") Integer id,
                               Model model) {

        if (id == null) {
            return "error/404";
        }

        PortfolioDetailsDTO portfolioDetails = portfolioService.selectPortfolioById(id);
        model.addAttribute("portfolio", portfolioDetails);

        return "portfolio-detail";
    }

    /**
     * 获取所有作品集API
     */
    @GetMapping("/api/portfolios")
    @ResponseBody
    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
    public ResponseEntity<List<PortfolioDetailsDTO>> getAllPortfolios() {
        List<PortfolioDetailsDTO> allPortfolios = portfolioService.selectAllPortfolios();
        return ResponseEntity.ok(allPortfolios);
    }

}
