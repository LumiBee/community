package com.lumibee.hive.service;

import com.lumibee.hive.model.Tag;

import java.util.List;
import java.util.Set;

public interface TagService {

    Tag selectOrCreateTag(String tagName);
    Set<Tag> selectOrCreateTags(List<String> tagNames);
    List<Tag> selectTagsByArticleId(int articleId);
    List<Tag> selectAllTags();
    void insertTagArticleRelation(Integer articleId, Integer tagId);
    Tag selectTagBySlug(String slug);
}
