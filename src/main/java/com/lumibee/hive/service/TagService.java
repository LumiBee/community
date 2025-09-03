package com.lumibee.hive.service;

import java.util.List;
import java.util.Set;

import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.model.Tag;

public interface TagService {

    Tag selectOrCreateTag(String tagName);
    void incrementArticleCount(Integer tagId);
    Set<Tag> selectOrCreateTags(List<String> tagNames);
    List<Tag> selectTagsByArticleId(Integer articleId);
    List<TagDTO> selectAllTags();
    void insertTagArticleRelation(Integer articleId, Integer tagId);
    TagDTO selectTagBySlug(String slug);
    TagDTO selectTagByName(String name);
}
