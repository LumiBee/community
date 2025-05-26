package com.lumibee.hive.service;

import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.model.Portfolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioMapper portfolioMapper;

    @Override
    public Portfolio selectOrCreatePortfolio(String portfolioName) {
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
            portfolioMapper.insert(portfolio);
        }

        return portfolio;
    }
}
