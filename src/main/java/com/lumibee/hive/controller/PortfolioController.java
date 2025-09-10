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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "作品集管理", description = "作品集相关的 API 接口")
public class PortfolioController {

    @Autowired private PortfolioMapper portfolioMapper;

    @Autowired private PortfolioService portfolioService;

    @Autowired private ArticleService articleService;

    @Autowired private UserService userService;

    /**
     * 获取单个作品集详情API
     */
    @GetMapping("/portfolio/{id}")
    @Operation(summary = "获取作品集详情", description = "根据ID获取指定作品集的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "作品集不存在")
    })
    public ResponseEntity<PortfolioDetailsDTO> getPortfolioById(
            @Parameter(description = "作品集ID") @PathVariable("id") Integer id) {
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
    @GetMapping("/portfolios")
    @Operation(summary = "获取所有作品集", description = "获取系统中所有作品集的列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<PortfolioDetailsDTO>> getAllPortfolios() {
        List<PortfolioDetailsDTO> allPortfolios = portfolioService.selectAllPortfolios();
        return ResponseEntity.ok(allPortfolios);
    }

    /**
     * 创建作品集API
     */
    @PostMapping("/portfolio")
    @Operation(summary = "创建作品集", description = "创建新的作品集")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<Portfolio> createPortfolio(
            @Parameter(description = "作品集创建请求") @RequestBody PortfolioCreateRequest request, 
            @AuthenticationPrincipal Principal principal) {
        
        if (request == null || request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 从当前登录用户获取用户ID
        User currentUser = userService.getCurrentUserFromPrincipal(principal);
        
        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String description = request.getDescription() != null ? request.getDescription().trim() : null;
            Portfolio portfolio = portfolioService.selectOrCreatePortfolio(request.getTitle().trim(), currentUser.getId(), description);
            if (portfolio != null) {
                return ResponseEntity.ok(portfolio);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
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
