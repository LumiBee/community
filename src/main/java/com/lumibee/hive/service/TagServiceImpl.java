package com.lumibee.hive.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.mapper.TagMapper;
import com.lumibee.hive.model.Tag;

@Service
public class TagServiceImpl implements TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

    @Autowired private TagMapper tagMapper;

    @Override
    @CacheEvict(value = "allTags", allEntries = true)
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
    @Transactional
    public void incrementArticleCount(Integer tagId) {
        if (tagId != null) {
            tagMapper.incrementArticleCount(tagId);
        }
    }

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public List<Tag> selectTagsByArticleId(Integer articleId) {
        return tagMapper.selectTagsByArticleId(articleId);
    }

    @Override
    @Cacheable(value = "allTags", unless = "#result == null or #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<TagDTO> selectAllTags() {
        try {
            List<TagDTO> result = tagMapper.selectAllTags();
            if (result == null) {
                logger.warn("selectAllTags returned null");
            }
            return result;
        } catch (Exception e) {
            logger.error("Error occurred while selecting all tags", e);
            return null;
        }
    }

    @Override
    @Transactional
    public void insertTagArticleRelation(Integer articleId, Integer tagId) {
        tagMapper.insertTagArticleRelation(articleId, tagId);
    }

    @Override
    @Cacheable(value = "tagDetails", key = "#slug", unless = "#result == null")
    @Transactional(readOnly = true)
    public TagDTO selectTagBySlug(String slug) {
        // 参数验证
        if (slug == null || slug.trim().isEmpty()) {
            logger.warn("selectTagBySlug called with null or empty slug");
            return null;
        }
        
        try {
            TagDTO result = tagMapper.selectBySlug(slug.trim());
            if (result == null) {
                logger.debug("No tag found for slug: {}", slug);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error occurred while selecting tag by slug: {}", slug, e);
            return null;
        }
    }

}
