package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.model.Article;
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
    @Select("SELECT article_id, title, slug, excerpt, view_count, likes, a.gmt_modified, user_id, u.name AS userName, u.avatar_url " + // 按需选择字段
            "FROM articles a " +
            "LEFT JOIN user u ON u.id = user_id " +
            "WHERE portfolio_id = #{portfolioId} AND status = 'published' " +
            "ORDER BY gmt_modified DESC")
    List<ArticleExcerptDTO> selectArticlesByPortfolioId(@Param("portfolioId") Integer portfolioId);
    @Select("SELECT article_id, title, slug, excerpt, view_count, likes, gmt_modified from articles " +
            "WHERE user_id = #{id} AND deleted = 0 AND status = 'published' " +
            "ORDER BY gmt_modified DESC")
    List<ArticleExcerptDTO> getArticlesByUserId(@Param("id") Long id);
    @Select("SELECT COUNT(*) FROM articles WHERE portfolio_id = #{portfolioId}")
    Integer countArticlesByPortfolioId(@Param("portfolioId") Integer portfolioId);
    @Select("SELECT a.article_id, a.user_id, a.title, a.excerpt, a.slug, a.view_count, a.likes, a.gmt_modified, " +
            "u.name AS user_name, u.avatar_url " +
            "FROM articles a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.deleted = 0 AND a.status = 'published' " +
            "ORDER BY a.view_count DESC LIMIT #{limit}")
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
    @Select("SELECT count(*) from articles where user_id = #{id} and deleted = 0")
    Integer countArticlesByUserId(@Param("id")Long id);
    @Select("SELECT a.article_id, a.title, a.excerpt, a.slug " +
            "FROM articles a " +
            "WHERE a.title = #{title} AND a.status = 'published' ")
    @Results({
            @Result(property = "articleId", column = "article_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "slug", column = "slug"),
            @Result(property = "excerpt", column = "excerpt"),
    })
    List<ArticleExcerptDTO> selectFeaturedArticles(@Param("title") String title);

    @Select("SELECT slug, gmt_modified from articles WHERE status = 'published' AND deleted = 0")
    List<ArticleExcerptDTO> selectSitemapDetails();
}
