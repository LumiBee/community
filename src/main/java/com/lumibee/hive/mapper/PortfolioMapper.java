package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.Portfolio;
import org.apache.ibatis.annotations.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PortfolioMapper extends BaseMapper<Portfolio> {
    @Select("SELECT * FROM portfolio WHERE name = #{portfolioName} LIMIT 1")
    Portfolio selectByName(@Param("portfolioName") String portfolioName);
    @Select("SELECT COUNT(*) FROM articles WHERE articles.portfolio_id = #{portfolioId}")
    Integer countArticlesByPortfolioId(@Param("portfolioId") Integer portfolioId);

    @Select("<script>" +
            "SELECT portfolio_id, COUNT(*) as article_count FROM articles " +
            "WHERE portfolio_id IN " +
            "<foreach item='item' index='index' collection='portfolioIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach> " +
            "AND deleted = 0 AND status = 'published' " +
            "GROUP BY portfolio_id" +
            "</script>")
    List<Map<String, Object>> countArticlesForPortfolioIdsInternal(@Param("portfolioIds") List<Integer> portfolioIds);

    default Map<Integer, Integer> countArticlesForPortfolios(List<Integer> portfolioIds) {
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> counts = countArticlesForPortfolioIdsInternal(portfolioIds);
        return counts.stream()
                .collect(Collectors.toMap(
                        map -> ((Number) map.get("portfolio_id")).intValue(),
                        map -> ((Number) map.get("article_count")).intValue()
                ));
    }

    @Select("Select slug, gmt_modified from portfolio where deleted = 0")
    List<String> selectSitemapDetails();
}
