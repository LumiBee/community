package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.articleLikes;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ArticleLikesMapper extends BaseMapper<articleLikes> {
    @Select("SELECT * from article_likes WHERE user_id = #{userId} AND article_id = #{articleId}")
    Integer toggleLike(@Param("userId") long userId,@Param("articleId") int articleId);
    @Insert("INSERT IGNORE INTO article_likes (user_id, article_id) VALUES (#{userId}, #{articleId})")
    void insertLike(@Param("userId") long userId,@Param("articleId") int articleId);
    @Delete("DELETE FROM article_likes WHERE user_id = #{userId} AND article_id = #{articleId}")
    void deleteLike(long userId, int articleId);
}
