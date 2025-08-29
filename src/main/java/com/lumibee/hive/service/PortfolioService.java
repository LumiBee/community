package com.lumibee.hive.service;

import java.util.List;

import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.model.Portfolio;

public interface PortfolioService {
    Portfolio selectOrCreatePortfolio(String portfolioName, Long userId);
    Portfolio selectOrCreatePortfolio(String portfolioName, Long userId, String description);
    List<PortfolioDetailsDTO> selectAllPortfolios();
    PortfolioDetailsDTO selectPortfolioById(Integer id);
    void updatePortfolioGmt(Integer id, Long userId);
}
