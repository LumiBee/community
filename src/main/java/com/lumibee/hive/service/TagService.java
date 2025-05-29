package com.lumibee.hive.service;

import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.model.Tag;

import java.util.List;
import java.util.Set;

public interface TagService {

    Tag selectOrCreateTag(String tagName);
    void incrementArticleCount(Integer tagId);
    Set<Tag> selectOrCreateTags(List<String> tagNames);
    List<Tag> selectTagsByArticleId(Integer articleId);
    List<TagDTO> selectAllTags();
    void insertTagArticleRelation(Integer articleId, Integer tagId);
    TagDTO selectTagBySlug(String slug);
}
