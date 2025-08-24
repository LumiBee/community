package com.lumibee.hive.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;

@RestController
public class PortfolioController {

    @Autowired private PortfolioMapper portfolioMapper;

    @Autowired private PortfolioService portfolioService;

    @Autowired private ArticleService articleService;

    /**
     * 重定向作品集详情页到Vue SPA
     */
    @GetMapping("/portfolio/{id}")
    public ResponseEntity<Void> redirectToPortfolioDetailSPA(@PathVariable("id") Integer id) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/portfolio/" + id)
                .build();
    }

    /**
     * 获取单个作品集详情API
     */
    @GetMapping("/api/portfolio/{id}")
    public ResponseEntity<PortfolioDetailsDTO> getPortfolioById(@PathVariable("id") Integer id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        PortfolioDetailsDTO portfolioDetails = portfolioService.selectPortfolioById(id);
        if (portfolioDetails == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(portfolioDetails);
    }

    /**
     * 获取所有作品集API
     */
    @GetMapping("/api/portfolios")
    public ResponseEntity<List<PortfolioDetailsDTO>> getAllPortfolios() {
        List<PortfolioDetailsDTO> allPortfolios = portfolioService.selectAllPortfolios();
        return ResponseEntity.ok(allPortfolios);
    }

}
