package com.lumibee.hive.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.mapper.TagMapper;
import com.lumibee.hive.model.Tag;
import com.lumibee.hive.constant.CacheNames;

/**
 * 标签服务实现类
 * 负责标签相关的业务逻辑处理，包括标签的增删改查、文章标签关系管理等
 */
@Service
@Log4j2
public class TagServiceImpl implements TagService {

    @Autowired private TagMapper tagMapper;
    @Autowired private RedisCacheService redisCacheService;
    @Autowired private CacheMonitoringService cacheMonitoringService;

    /**
     * 增加标签的文章计数
     * @param tagId 标签ID
     */
    @Override
    @Transactional
    public void incrementArticleCount(Integer tagId) {
        if (tagId != null) {
            tagMapper.incrementArticleCount(tagId);
            
            // 清除标签相关缓存
            try {
                redisCacheService.clearAllTagListCaches();
            } catch (Exception e) {
                log.error("清除标签相关缓存时出错: {}", e.getMessage());
            }
        }
    }

    /**
     * 选择或创建标签
     * @param tagNames 标签名称列表
     * @return 标签集合
     */
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

    /**
     * 根据文章ID获取标签列表
     * @param articleId 文章ID
     * @return 标签列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Tag> selectTagsByArticleId(Integer articleId) {
        return tagMapper.selectTagsByArticleId(articleId);
    }

    /**
     * 获取所有标签列表
     * @return 标签DTO列表
     */
    @Override
    @Cacheable(value = CacheNames.ALL_TAGS, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).allTags()")
    @Transactional(readOnly = true)
    public List<TagDTO> selectAllTags() {
        try {
            List<TagDTO> result = tagMapper.selectAllTags();
            if (result == null) {
                log.warn("selectAllTags returned null");
            }
            return result;
        } catch (Exception e) {
            log.error("Error occurred while selecting all tags", e);
            return null;
        }
    }

    /**
     * 插入文章标签关系
     * @param articleId 文章ID
     * @param tagId 标签ID
     */
    @Override
    @Transactional
    public void insertTagArticleRelation(Integer articleId, Integer tagId) {
        tagMapper.insertTagArticleRelation(articleId, tagId);
        
        // 清除标签相关缓存
        try {
            redisCacheService.clearAllTagListCaches();
        } catch (Exception e) {
            log.error("清除标签相关缓存时出错: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO selectTagBySlug(String slug) {
        // 参数验证
        if (slug == null || slug.trim().isEmpty()) {
            log.warn("selectTagBySlug called with null or empty slug");
            return null;
        }

        try {
            TagDTO result = tagMapper.selectBySlug(slug.trim());
            if (result == null) {
                log.debug("No tag found for slug: {}", slug);
            }
            return result;
        } catch (Exception e) {
            log.error("Error occurred while selecting tag by slug: {}", slug, e);
            return null;
        }
    }

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

}
