package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.dto.PortfolioDTO;
import com.lumibee.hive.model.Portfolio;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface PortfolioMapper extends BaseMapper<Portfolio> {
    @Select("SELECT * FROM portfolio WHERE name = #{portfolioName} LIMIT 1")
    Portfolio selectByName(@Param("portfolioName") String portfolioName);
    
    @Select("SELECT p.id, p.name, p.slug, p.description, p.gmt_create, p.gmt_modified, p.cover_img_url, p.user_id " +
            "FROM portfolio p " +
            "LEFT JOIN user u " +
            "ON p.user_id = u.id " +
            "WHERE p.slug = #{slug} LIMIT 1")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "slug", column = "slug"),
            @Result(property = "description", column = "description"),
            @Result(property = "gmtCreate", column = "gmt_create"),
            @Result(property = "gmtModified", column = "gmt_modified"),
            @Result(property = "coverImgUrl", column = "cover_img_url"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "user", column = "user_id", one = @One(select = "com.lumibee.hive.mapper.UserMapper.selectById")),
            @Result(property = "articleCount", column = "id", one = @One(select = "com.lumibee.hive.mapper.ArticleMapper.countArticlesByPortfolioId"))
    })
    Portfolio selectBySlug(@Param("slug") String slug);
    @Select("SELECT COUNT(*) FROM portfolio LEFT JOIN user ON portfolio.user_id = user.id WHERE user.id = #{userId}")
    Integer countArticlesByPortfolioId(@Param("userId") Integer userId);
}
