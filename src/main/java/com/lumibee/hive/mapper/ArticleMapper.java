package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    @Select("SELECT EXISTS(SELECT 1 FROM articles a WHERE slug = #{slug})")
    boolean selectBySlug(String slug);
    @Delete("DELETE FROM article_tags WHERE article_id = #{articleId}")
    void deleteArticleTagByArticleId(@Param("articleId")Integer articleId);
    @Update("update articles set likes = likes + #{likes} where article_id = #{articleId}")
    void updateArticleLikes(@Param("articleId") Integer articleId, @Param("likes") int likes);
    @Select("SELECT likes FROM articles WHERE article_id = #{articleId}")
    Integer countLikes(@Param("articleId") Integer articleId);
    @Update("UPDATE articles SET view_count = view_count + 1 WHERE article_id = #{articleId}")
    void incrementViewCount(@Param("articleId") Integer articleId);
    @Select("SELECT article_id, title, slug, excerpt, view_count, likes, gmt_modified, user_id " + // 按需选择字段
            "FROM articles " +
            "WHERE portfolio_id = #{portfolioId} AND status = 'published' " +
            "ORDER BY gmt_modified DESC")
    List<Article> selectArticlesByPortfolioId(@Param("portfolioId") Integer portfolioId);
    @Select("SELECT COUNT(*) FROM articles WHERE portfolio_id = #{portfolioId}")
    Integer countArticlesByPortfolioId(@Param("portfolioId") Integer portfolioId);
    @Select("SELECT a.article_id, a.user_id, a.title, a.excerpt, a.slug, a.view_count, a.likes, a.gmt_modified, " +
            "u.name AS user_name, u.avatar_url " +
            "FROM articles a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.deleted = 0 AND a.status = 'published' " +
            "ORDER BY a.gmt_modified DESC LIMIT #{limit}")
    @Results({
            @Result(property = "articleId", column = "article_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "slug", column = "slug"),
            @Result(property = "excerpt", column = "excerpt"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "likes", column = "likes"),
            @Result(property = "gmtModified", column = "gmt_modified"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "avatarUrl", column = "avatar_url")
    })
    List<ArticleExcerptDTO> selectArticleSummaries(@Param("limit") Integer limit);
    @Select("SELECT a.article_id, a.user_id, a.title, a.slug, a.excerpt, a.gmt_modified, " +
            "u.name AS user_name, u.avatar_url " +
            "from articles a " +
            "LEFT JOIN article_tags at ON a.article_id = at.article_id " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE at.tag_id = #{tagId} " +
            "ORDER BY a.view_count DESC")
    @Results(id = "getArticlesByTagId", value ={
            @Result(property = "articleId", column = "article_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "title", column = "title"),
            @Result(property = "slug", column = "slug"),
            @Result(property = "avatarUrl", column = "avatar_url"),
            @Result(property = "excerpt", column = "excerpt"),
            @Result(property = "gmtModified", column = "gmt_modified")
    })
    List<ArticleExcerptDTO> getArticlesByTagId(@Param("tagId") Integer tagId);
}
