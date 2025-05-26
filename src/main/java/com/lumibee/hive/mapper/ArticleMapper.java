package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    @Select("SELECT EXISTS(SELECT 1 FROM articles a WHERE slug = #{slug})")
    boolean selectBySlug(String slug);
    @Delete("DELETE FROM article_tags WHERE article_id = #{articleId}")
    void deleteArticleTagByArticleId(@Param("articleId")Integer articleId);
    @Select("SELECT * FROM articles WHERE slug = #{slug}")
    Article findBySlug(String slug);
    @Update("update articles set likes = likes + #{likes} where article_id = #{articleId}")
    void updateArticleLikes(@Param("articleId") Integer articleId, @Param("likes") int likes);
    @Select("SELECT likes FROM articles WHERE article_id = #{articleId}")
    Integer countLikes(@Param("articleId") Integer articleId);
    @Update("UPDATE articles SET view_count = view_count + 1 WHERE article_id = #{articleId}")
    void incrementViewCount(Integer articleId);
    @Select("SELECT * FROM articles ORDER BY view_count DESC LIMIT 5")
    List<Article> getTopArticles();
}
