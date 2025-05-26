package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.Portfolio;
import org.apache.ibatis.annotations.Select;

public interface PortfolioMapper extends BaseMapper<Portfolio> {
    @Select("SELECT * FROM portfolio WHERE name = #{portfolioName} LIMIT 1")
    Portfolio selectByName(String portfolioName);
}
