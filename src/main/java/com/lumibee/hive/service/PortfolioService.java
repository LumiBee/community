package com.lumibee.hive.service;

import com.lumibee.hive.dto.PortfolioDetailsDTO;
import com.lumibee.hive.model.Portfolio;

import java.util.List;

public interface PortfolioService {
    Portfolio selectOrCreatePortfolio(String portfolioName, Long userId);
    List<PortfolioDetailsDTO> selectAllPortfolios();
    PortfolioDetailsDTO selectPortfolioById(Integer id);
    void updatePortfolioGmt(Integer id, Long userId);
}
