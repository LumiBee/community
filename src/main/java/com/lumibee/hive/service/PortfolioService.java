package com.lumibee.hive.service;

import java.util.List;

import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.model.Portfolio;

public interface PortfolioService {
    Portfolio selectOrCreatePortfolio(String portfolioName, long userId);

    Portfolio selectOrCreatePortfolio(String portfolioName, long userId, String description);

    List<PortfolioDetailsDTO> selectAllPortfolios();

    PortfolioDetailsDTO selectPortfolioById(int id);

    void updatePortfolioGmt(int id, long userId);
}
