package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.model.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Select("SELECT * FROM tags WHERE slug = #{slug} LIMIT 1")
    TagDTO selectBySlug(@Param("slug") String slug);
    @Select("SELECT * FROM tags WHERE name = #{name} LIMIT 1")
    TagDTO selectByName(@Param("name") String name);
    @Select("SELECT t.tag_id, t.name, t.slug FROM tags t " +
            "INNER JOIN article_tags at ON t.tag_id = at.tag_id " +
            "WHERE at.article_id = #{articleId}")
    List<Tag> selectTagsByArticleId(@Param("articleId") Integer articleId);
    @Select("SELECT * from tags ORDER BY article_count DESC LIMIT 20")
    List<TagDTO> selectAllTags();
    @Update("UPDATE tags SET article_count =#{currentCount} WHERE tag_id = #{tagId}")
    void updateArticleCount(@Param("tagId") Integer tagId, @Param("currentCount") Integer currentCount);
    @Insert("INSERT IGNORE into article_tags (article_id, tag_id) VALUES (#{articleId}, #{tagId})")
    void insertTagArticleRelation(@Param("articleId") Integer articleId, @Param("tagId") Integer tagId);
    @Update("UPDATE tags SET article_count = article_count + 1 WHERE tag_id = #{tagId}")
    void incrementArticleCount(@Param("tagId") Integer tagId);
}
