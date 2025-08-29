package com.lumibee.hive.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.model.Portfolio;
import com.lumibee.hive.model.User;
import com.lumibee.hive.service.ArticleService;
import com.lumibee.hive.service.PortfolioService;
import com.lumibee.hive.service.UserService;

@RestController
public class PortfolioController {

    @Autowired private PortfolioMapper portfolioMapper;

    @Autowired private PortfolioService portfolioService;

    @Autowired private ArticleService articleService;

    @Autowired private UserService userService;

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
        System.out.println("=== 获取所有作品集API被调用 ===");
        List<PortfolioDetailsDTO> allPortfolios = portfolioService.selectAllPortfolios();
        System.out.println("返回作品集数量: " + (allPortfolios != null ? allPortfolios.size() : 0));
        if (allPortfolios != null) {
            for (PortfolioDetailsDTO p : allPortfolios) {
                System.out.println("返回作品集: ID=" + p.getId() + ", name=" + p.getName() + ", userName=" + p.getUserName());
            }
        }
        return ResponseEntity.ok(allPortfolios);
    }

    /**
     * 创建作品集API
     */
    @PostMapping("/api/portfolio")
    public ResponseEntity<Portfolio> createPortfolio(@RequestBody PortfolioCreateRequest request, 
                                                   @AuthenticationPrincipal Principal principal) {
        System.out.println("=== 创建作品集API被调用 ===");
        System.out.println("Principal: " + principal);
        System.out.println("Request: " + request);
        
        if (request == null || request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            System.out.println("请求数据无效");
            return ResponseEntity.badRequest().build();
        }

        // 从当前登录用户获取用户ID
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        System.out.println("当前用户: " + currentUser);
        
        if (currentUser == null || currentUser.getId() == null) {
            System.out.println("用户未认证或用户ID为空");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String description = request.getDescription() != null ? request.getDescription().trim() : null;
            System.out.println("创建作品集 - 标题: " + request.getTitle() + ", 描述: " + description + ", 用户ID: " + currentUser.getId());
            
            Portfolio portfolio = portfolioService.selectOrCreatePortfolio(request.getTitle().trim(), currentUser.getId(), description);
            if (portfolio != null) {
                System.out.println("作品集创建成功: " + portfolio.getId());
                return ResponseEntity.ok(portfolio);
            } else {
                System.out.println("作品集创建失败");
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            System.out.println("创建作品集时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 作品集创建请求DTO
     */
    public static class PortfolioCreateRequest {
        private String title;
        private String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
