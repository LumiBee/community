package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Select("SELECT * FROM tags WHERE slug = #{slug} LIMIT 1")
    Tag selectBySlug(String slug);
    @Select("SELECT * FROM tags WHERE name = #{name} LIMIT 1")
    Tag selectByName(String name);
    @Select("SELECT * FROM tags INNER JOIN article_tags at ON tags.tag_id = at.tag_id WHERE at.article_id = #{articleId}")
    List<Tag> selectByArticleId(Integer articleId);
    @Select("SELECT * from tags ORDER BY article_count DESC LIMIT 20")
    List<Tag> selectAllTags();
}
