package com.lumibee.hive.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.Portfolio;
import com.lumibee.hive.model.User;
import com.lumibee.hive.constant.CacheNames;

/**
 * 作品集服务实现类
 * 负责作品集相关的业务逻辑处理，包括作品集的增删改查、文章关联等
 */
@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired private PortfolioMapper portfolioMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ArticleMapper articleMapper;
    @Autowired private RedisCacheService redisCacheService;

    @Override
    @Transactional
    public Portfolio selectOrCreatePortfolio(String portfolioName, Long userId) {
        return selectOrCreatePortfolio(portfolioName, userId, "Portfolio for " + portfolioName);
    }

    @Override
    @Transactional
    public Portfolio selectOrCreatePortfolio(String portfolioName, Long userId, String description) {
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
            portfolio.setDescription(description != null ? description : "Portfolio for " + portfolioName);
            portfolio.setUserId(userId);
            portfolioMapper.insert(portfolio);
            
            // 清除作品集相关缓存
            try {
                redisCacheService.clearPortfolioDetailCaches(portfolio.getId());
            } catch (Exception e) {
                System.err.println("清除作品集缓存时出错: " + e.getMessage());
            }
        }

        return portfolio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioDetailsDTO> selectAllPortfolios() {
        System.out.println("=== 开始获取所有作品集 ===");
        QueryWrapper<Portfolio> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).orderByDesc("gmt_modified");
        List<Portfolio> portfolios = portfolioMapper.selectList(queryWrapper);
        
        System.out.println("从数据库查询到的作品集数量: " + (portfolios != null ? portfolios.size() : 0));
        if (portfolios != null) {
            for (Portfolio p : portfolios) {
                System.out.println("作品集: ID=" + p.getId() + ", name=" + p.getName() + ", userId=" + p.getUserId());
            }
        }

        if (portfolios == null || portfolios.isEmpty()) {
            System.out.println("没有找到作品集，返回空列表");
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
    @Cacheable(value = CacheNames.PORTFOLIO_DETAIL, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).portfolioDetail(#id)")
    @Transactional(readOnly = true)
    public PortfolioDetailsDTO selectPortfolioById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }

        QueryWrapper<Portfolio> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("deleted", 0);
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

        List<ArticleExcerptDTO> articles = articleMapper.getArticlesByPortfolioId(portfolio.getId());
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
