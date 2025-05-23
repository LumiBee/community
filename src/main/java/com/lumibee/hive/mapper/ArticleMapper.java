package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.Article;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    @Select("SELECT EXISTS(SELECT 1 FROM articles WHERE slug = #{slug})")
    boolean selectBySlug(String slug);
    @Insert("INSERT IGNORE INTO article_tags (article_id, tag_id) VALUES (#{articleId}, #{tagId})")
    void insertArticleTag(@Param("articleId")Integer articleId,@Param("tagId")Integer tagId);
    @Delete("DELETE FROM article_tags WHERE article_id = #{articleId}")
    void deleteArticleTagByArticleId(@Param("articleId")Integer articleId);
    @Select("SELECT * FROM articles WHERE slug = #{slug}")
    Article findBySlug(String slug);
}
