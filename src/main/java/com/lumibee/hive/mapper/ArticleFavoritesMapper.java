package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.ArticleFavorites;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleFavoritesMapper extends BaseMapper<ArticleFavorites> {
    @Select("SELECT COUNT(*) FROM article_favorites WHERE favorite_id = #{id}")
    Integer countArticlesInFavorite(@Param("id") Long id);
    @Select("SELECT a.article_id, a.title, a.excerpt, a.user_id, a.gmt_create AS gmtCreate, a.gmt_modified AS gmtModified " +
            "FROM article_favorites af " +
            "JOIN articles a ON af.article_id = a.article_id " +
            "WHERE af.favorite_id = #{id}")
    List<ArticleExcerptDTO> selectArticlesByFavoriteId(@Param("id") Long id);
    @Select("SELECT COUNT(*) FROM article_favorites WHERE user_id = #{userId} AND article_id = #{articleId}")
    Integer selectIfFavoriteExists(@Param("userId") Long userId, @Param("articleId") Integer articleId);
    @Select("SELECT COUNT(*) FROM article_favorites WHERE article_id = #{articleId}")
    int countArticlesFavorited(@Param("articleId") Integer articleId);
    @Delete("DELETE FROM article_favorites WHERE user_id = #{userId} AND article_id = #{articleId}")
    Integer deleteByArticleId(@Param("articleId") Integer articleId,@Param("userId") Long userId);
    @Delete("DELETE FROM article_favorites WHERE user_id = #{userId} AND favorite_id = #{favoriteId} AND article_id = #{articleId}")
    Integer deleteByArticleIdAndFavoriteId(@Param("articleId") Integer articleId, @Param("favoriteId") Long favoriteId, @Param("userId") Long userId);
    @Select("SELECT favorite_id FROM article_favorites WHERE user_id = #{userId} AND article_id = #{articleId}")
    List<Long> selectFavoriteIdsByArticleIdAndUserId(@Param("articleId") Integer articleId, @Param("userId") Long userId);
}
