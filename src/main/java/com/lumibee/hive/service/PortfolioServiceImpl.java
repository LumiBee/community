package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.Portfolio;
import com.lumibee.hive.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired private PortfolioMapper portfolioMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ArticleMapper articleMapper;

    @Override
    @Transactional
    public Portfolio selectOrCreatePortfolio(String portfolioName, Long userId) {
        if (portfolioName == null || portfolioName.isEmpty()) {
            return null; // 如果传入的portfolioName为空，直接返回null
        }

        String trimmedPortfolioName = portfolioName.trim();
        QueryWrapper<Portfolio> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", trimmedPortfolioName).eq("user_id", userId);
        Portfolio portfolio = portfolioMapper.selectOne(queryWrapper);

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
    @Transactional(readOnly = true)
    public List<PortfolioDetailsDTO> selectAllPortfolios() {
        QueryWrapper<Portfolio> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).orderByDesc("gmt_modified");
        List<Portfolio> portfolios = portfolioMapper.selectList(queryWrapper);

        if (portfolios == null || portfolios.isEmpty()) {
            return List.of();
        }

        // 获取所有用户ID
        List<Long> userIds = portfolios.stream()
                .map(Portfolio::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));
        }

        List<Integer> portfolioIds = portfolios.stream().map(Portfolio::getId).collect(Collectors.toList());
        Map<Integer, Integer> articleCountsMap = Collections.emptyMap();
        if(!portfolioIds.isEmpty()){
            articleCountsMap = portfolioMapper.countArticlesForPortfolios(portfolioIds);
        }

        List<PortfolioDetailsDTO> portfolioDTOs = new ArrayList<>();
        for (Portfolio portfolio : portfolios) {
            PortfolioDetailsDTO portfolioDTO = new PortfolioDetailsDTO();
            BeanUtils.copyProperties(portfolio, portfolioDTO);

            if (portfolio.getUserId() != null) {
                User user = userMap.get(portfolio.getUserId());
                if (user != null) {
                    portfolioDTO.setAvatarUrl(user.getAvatarUrl());
                    portfolioDTO.setUserName(user.getName());
                } else {
                    portfolioDTO.setUserName("未知用户");
                }
            }
            portfolioDTO.setArticlesCount(articleCountsMap.getOrDefault(portfolio.getId(), 0));
            portfolioDTO.setArticles(Collections.emptyList());

            portfolioDTOs.add(portfolioDTO);
        }

        return portfolioDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioDetailsDTO selectPortfolioBySlug(String slug) {
        if (slug == null || slug.isEmpty()) {
            throw new IllegalArgumentException("slug不能为空");
        }

        QueryWrapper<Portfolio> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("slug", slug).eq("deleted", 0);
        Portfolio portfolio = portfolioMapper.selectOne(queryWrapper);

        if (portfolio == null || portfolio.getId() == null) {
            throw new IllegalArgumentException("没有找到作品集，请检查slug是否正确");
        }
        
        PortfolioDetailsDTO dto = new PortfolioDetailsDTO();
        BeanUtils.copyProperties(portfolio, dto);

        if (portfolio.getUserId() != null) {
            User user = userMapper.selectById(portfolio.getUserId()); 
            if (user != null) {
                dto.setUserName(user.getName());
                dto.setAvatarUrl(user.getAvatarUrl());
            }
        }

        List<ArticleExcerptDTO> articles = articleMapper.selectArticlesByPortfolioId(portfolio.getId());
        dto.setArticles(articles != null ? articles : Collections.emptyList());

        Integer articleCount = articleMapper.countArticlesByPortfolioId(portfolio.getId());
        dto.setArticlesCount(articleCount != null ? articleCount : 0);

        return dto;
    }

    @Override
    @Transactional
    public void updatePortfolioGmt(Integer id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("id和userId不能为空");
        }
        Portfolio portfolio = portfolioMapper.selectById(id);
        if (portfolio == null || !portfolio.getUserId().equals(userId)) {
            throw new IllegalArgumentException("作品集不存在或不属于当前用户");
        }
        portfolio.setGmtModified(LocalDateTime.now());
    }


}
