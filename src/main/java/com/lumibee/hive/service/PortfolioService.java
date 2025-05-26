package com.lumibee.hive.service;

import com.lumibee.hive.model.Portfolio;

public interface PortfolioService {
    Portfolio selectOrCreatePortfolio(String portfolioName);
}
