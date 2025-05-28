package com.lumibee.hive.service;

import com.lumibee.hive.model.Portfolio;

import java.util.List;

public interface PortfolioService {
    Portfolio selectOrCreatePortfolio(String portfolioName);
    List<Portfolio> selectAllPortfolios();
    Portfolio selectPortfolioBySlug(String slug);
}
