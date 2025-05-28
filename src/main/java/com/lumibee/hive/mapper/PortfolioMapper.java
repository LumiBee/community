package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.Portfolio;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PortfolioMapper extends BaseMapper<Portfolio> {
    @Select("SELECT * FROM portfolio WHERE name = #{portfolioName} LIMIT 1")
    Portfolio selectByName(@Param("portfolioName") String portfolioName);
    @Select("SELECT * FROM portfolio")
    List<Portfolio> selectAll();
    @Select("SELECT * FROM portfolio WHERE slug = #{slug} LIMIT 1")
    Portfolio selectBySlug(@Param("slug") String slug);
}
