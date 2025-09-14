package com.lumibee.hive.service;

import java.time.LocalDateTime;
import java.util.*;
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

/**
 * 作品集服务实现类
 * 负责作品集相关的业务逻辑处理，包括作品集的增删改查、文章关联等
 */
@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired private PortfolioMapper portfolioMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ArticleMapper articleMapper;
    @Autowired private RedisClearCacheService redisClearCacheService;
    @Autowired private RedisMonitoringService redisMonitoringService;
    @Autowired private RedisCounterService redisCounterService;

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
            
            // 更新 Redis 计数器
            try {
                redisCounterService.incrementUserPortfolio(userId);
            } catch (Exception e) {
                System.err.println("更新 Redis 用户作品集计数时出错: " + e.getMessage());
            }
            
            // 清除作品集相关缓存
            try {
                redisClearCacheService.clearPortfolioDetailCaches(portfolio.getId());
            } catch (Exception e) {
                System.err.println("清除作品集缓存时出错: " + e.getMessage());
            }
        }

        return portfolio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioDetailsDTO> selectAllPortfolios() {
        QueryWrapper<Portfolio> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).orderByDesc("gmt_modified");
        List<Portfolio> portfolios = portfolioMapper.selectList(queryWrapper);
        
        if (portfolios != null) {
            for (Portfolio p : portfolios) {
            }
        }

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
        Map<Integer, Integer> articleCountsMap = new HashMap<>();
        if(!portfolioIds.isEmpty()){
            // 从 Redis 获取作品集文章数量
            for (Integer portfolioId : portfolioIds) {
                try {
                    int articleCount = redisCounterService.getPortfolioArticleCount(portfolioId);
                    if (!redisCounterService.existsPortfolioArticleCount(portfolioId)) {
                        // 如果 Redis 中没有数据，从数据库获取并设置到 Redis
                        Integer dbCount = articleMapper.countArticlesByPortfolioId(portfolioId);
                        articleCount = dbCount != null ? dbCount : 0;
                        redisCounterService.setPortfolioArticleCount(portfolioId, articleCount);
                    }
                    articleCountsMap.put(portfolioId, articleCount);
                } catch (Exception e) {
                    // 如果 Redis 出错，从数据库获取
                    Integer dbCount = articleMapper.countArticlesByPortfolioId(portfolioId);
                    articleCountsMap.put(portfolioId, dbCount != null ? dbCount : 0);
                }
            }
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
    @Cacheable(value = "portfolios::detail", key = "#id")
    @Transactional(readOnly = true)
    public PortfolioDetailsDTO selectPortfolioById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }

        QueryWrapper<Portfolio> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("deleted", 0);
        Portfolio portfolio = portfolioMapper.selectOne(queryWrapper);

        if (portfolio == null || portfolio.getId() == null) {
            return null; // 返回null，会被缓存防止穿透
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
