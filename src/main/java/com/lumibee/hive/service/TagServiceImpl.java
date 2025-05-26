package com.lumibee.hive.service;

import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.mapper.TagMapper;
import com.lumibee.hive.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public Tag selectOrCreateTag(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return null;
        }

        String trimmedTagName = tagName.trim();
        Tag tag = tagMapper.selectByName(trimmedTagName);
        if (tag == null) {
            tag = new Tag();
            tag.setName(trimmedTagName);
            tag.setSlug(SlugGenerator.generateSlug(trimmedTagName));
            tag.setGmtCreate(LocalDateTime.now());
            tag.setArticleCount(1);
            tagMapper.insert(tag);
        }else {
            tagMapper.updateArticleCount(tag.getTagId(), tag.getArticleCount() + 1);
        }

        return tag;
    }

    @Override
    public Set<Tag> selectOrCreateTags(List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames != null) {
            for (String tagName : tagNames) {
                Tag tag = selectOrCreateTag(tagName);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    @Override
    public List<Tag> selectTagsByArticleId(int articleId) {
        return tagMapper.selectByArticleId(articleId);
    }

    @Override
    public List<Tag> selectAllTags() {
        return tagMapper.selectAllTags();
    }

    @Override
    public void insertTagArticleRelation(Integer articleId, Integer tagId) {
        tagMapper.insertTagArticleRelation(articleId, tagId);
    }
}
