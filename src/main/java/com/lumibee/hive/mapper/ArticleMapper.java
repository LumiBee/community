package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
    @Select("SELECT a.article_id, a.user_id, a.title, a.excerpt, a.slug, a.view_count, a.likes, a.allow_comments," +
            "u.name AS user_name, u.avatar_url " +
            "from articles a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "ORDER BY a.view_count DESC LIMIT #{limit}")
    @Results(id = "getArticlesLimit", value ={
            @Result(property = "articleId", column = "article_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "title", column = "title"),
            @Result(property = "slug", column = "slug"),
            @Result(property = "excerpt", column = "excerpt"),
            @Result(property = "avatarUrl", column = "avatar_url"),
            @Result(property = "viewCount", column = "view_count"),
            @Result(property = "likes", column = "likes")
    })
    List<Article> getArticlesLimit(@Param("limit") Integer limit);
    @Select("SELECT a.article_id, a.user_id, a.title, a.slug, a.content, a.excerpt, a.status, " +
            "a.gmt_create, a.gmt_modified, a.view_count, a.likes, a.allow_comments, a.portfolio_id, " +
            "u.name AS user_name, u.avatar_url " +
            "from articles a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "where slug = #{slug}")
    @Results(id = "articleDetails", value ={
        @Result(property = "articleId", column = "article_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "userName", column = "user_name"),
        @Result(property = "title", column = "title"),
        @Result(property = "slug", column = "slug"),
        @Result(property = "content", column = "content"),
        @Result(property = "excerpt", column = "excerpt"),
        @Result(property = "avatarUrl", column = "avatar_url"),
        @Result(property = "status", column = "status"),
        @Result(property = "gmtCreate", column = "gmt_create"),
        @Result(property = "gmtModified", column = "gmt_modified"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "likes", column = "likes"),
        @Result(property = "allowComments", column = "allow_comments"),
        @Result(property = "portfolioId", column = "portfolio_id"),
        @Result(property = "tags", column = "article_id", many = @Many(select = "com.lumibee.hive.mapper.TagMapper.selectByArticleId")),
        @Result(property = "portfolio", column = "portfolio_id", one = @One(select = "com.lumibee.hive.mapper.PortfolioMapper.selectById"))
    })
    Article findDetailsBySlug(@Param("slug") String slug);
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
    List<Article> getArticlesByTagId(@Param("tagId") Integer tagId);
}
