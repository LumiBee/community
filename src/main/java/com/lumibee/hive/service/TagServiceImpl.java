package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.mapper.TagMapper;
import com.lumibee.hive.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    @Transactional
    public Tag selectOrCreateTag(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return null;
        }

        // 根据 tagName 查询是否存在
        String trimmedTagName = tagName.trim();
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", trimmedTagName);
        Tag tag = tagMapper.selectOne(queryWrapper);

        if (tag != null) {
            return tag;
        }

        Tag newTag = new Tag();
        newTag.setName(trimmedTagName);
        newTag.setSlug(SlugGenerator.generateSlug(trimmedTagName));
        newTag.setGmtCreate(LocalDateTime.now());
        newTag.setArticleCount(0);

        try {
            tagMapper.insert(newTag);
            return newTag;
        } catch (Exception e) {
            // 如果插入失败，可能是因为并发问题，重新查询一次
            return tagMapper.selectOne(queryWrapper);
        }

    }

    @Override
    public void incrementArticleCount(Integer tagId) {
        if (tagId != null) {
            tagMapper.incrementArticleCount(tagId);
        }
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
    public List<Tag> selectTagsByArticleId(Integer articleId) {
        return tagMapper.selectTagsByArticleId(articleId);
    }

    @Override
    public List<TagDTO> selectAllTags() {
        return tagMapper.selectAllTags();
    }

    @Override
    public void insertTagArticleRelation(Integer articleId, Integer tagId) {
        tagMapper.insertTagArticleRelation(articleId, tagId);
    }

    @Override
    public TagDTO selectTagBySlug(String slug) {
        return tagMapper.selectBySlug(slug);
    }

}
