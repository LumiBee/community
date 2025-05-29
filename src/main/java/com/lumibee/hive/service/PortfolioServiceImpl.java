package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.model.Portfolio;
import com.lumibee.hive.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired private PortfolioMapper portfolioMapper;
    @Autowired private UserService userService;

    @Override
    public Portfolio selectOrCreatePortfolio(String portfolioName, Long userId) {
        if (portfolioName == null || portfolioName.isEmpty()) {
            return null; // 如果传入的portfolioName为空，直接返回null
        }
        Portfolio portfolio = portfolioMapper.selectByName(portfolioName);
        if (portfolio == null) {
            // 如果没有找到对应的Portfolio，则创建一个新的
            portfolio = new Portfolio();
            portfolio.setName(portfolioName);
            portfolio.setGmtCreate(LocalDateTime.now());
            portfolio.setGmtModified(LocalDateTime.now());
            portfolio.setSlug(SlugGenerator.generateSlug(portfolioName));
            portfolio.setDescription("Portfolio for " + portfolioName);
            portfolio.setUserId(userId);
            portfolioMapper.insert(portfolio);
        }

        return portfolio;
    }

    @Override
    public List<PortfolioDetailsDTO> selectAllPortfolios() {
        QueryWrapper<Portfolio> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("gmt_modified");
        List<Portfolio> portfolios = portfolioMapper.selectList(queryWrapper);

        if (portfolios == null || portfolios.isEmpty()) {
            return List.of();
        }

        List<PortfolioDetailsDTO> portfolioDTOs = new ArrayList<>();

        for (Portfolio portfolio : portfolios) {
            if (portfolio != null && portfolio.getId() != null) {
                // 将Portfolio转换为PortfolioDetailsDTO
                PortfolioDetailsDTO portfolioDTO = new PortfolioDetailsDTO();
                BeanUtils.copyProperties(portfolio, portfolioDTO);

                // 获取用户信息
                User user = userService.selectById(portfolio.getUserId());
                portfolioDTO.setAvatarUrl(user.getAvatarUrl());
                portfolioDTO.setUserName(user.getName());

                // 获取文章数量
                Integer articleCount = portfolioMapper.countArticlesByPortfolioId(portfolio.getId());
                portfolioDTO.setArticlesCount(articleCount != null ? articleCount : 0);

                portfolioDTOs.add(portfolioDTO);
            }
        }

        return portfolioDTOs;
    }

    @Override
    public Portfolio selectPortfolioBySlug(String slug) {
        return portfolioMapper.selectBySlug(slug);
    }

}
