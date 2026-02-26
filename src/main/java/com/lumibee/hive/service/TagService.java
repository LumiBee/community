package com.lumibee.hive.service;

import java.util.List;
import java.util.Set;

import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.model.Tag;

public interface TagService {

    void incrementArticleCount(int tagId);

    Set<Tag> selectOrCreateTags(List<String> tagNames);

    List<Tag> selectTagsByArticleId(int articleId);

    List<TagDTO> selectAllTags();

    void insertTagArticleRelation(int articleId, int tagId);

    TagDTO selectTagBySlug(String slug);
}
