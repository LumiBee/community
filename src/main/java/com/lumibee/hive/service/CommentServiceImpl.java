package com.lumibee.hive.service;

import com.lumibee.hive.dto.CommentDTO;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.mapper.CommentMapper;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.Comments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 评论服务实现类
 * 负责评论相关的业务逻辑处理，包括评论的增删改查、评论树结构管理等
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired private CommentMapper commentMapper;
    @Autowired private ArticleMapper articleMapper;
    @Autowired private CacheManager cacheManager;
    @Autowired private RedisMonitoringService redisMonitoringService;

    @Override
    @Cacheable(value = "comments::list::article", key = "#articleId")
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByArticleId(Integer articleId) {
        // 1. 获取所有顶级评论
        List<CommentDTO> topLevelComments = commentMapper.getTopCommentsByArticleId(articleId);

        // 检查是否为null
        if (topLevelComments == null) {
            return Collections.emptyList();
        }

        List<Long> topLevelCommentIds = topLevelComments.stream()
                .map(CommentDTO::getId)
                .collect(Collectors.toList());

        List<CommentDTO> allReplies = Collections.emptyList();
        if (!topLevelCommentIds.isEmpty()) {
            allReplies = commentMapper.getAllRepliesByRootIds(topLevelCommentIds);
        }

        Map<Long, List<CommentDTO>> repliesMap = allReplies.stream()
                .collect(Collectors.groupingBy(CommentDTO::getRootCommentId));

        topLevelComments.forEach(repComment -> {
            List<CommentDTO> replies = repliesMap.getOrDefault(repComment.getId(), Collections.emptyList());
            repComment.setReplies(replies);
        });


        return topLevelComments;
    }

    @Override
    @Transactional
    public Comments addComment(Integer articleId, String content, Long userId, Long parentId) {
        // 验证参数
        if (articleId == null || content == null || content.trim().isEmpty() || userId == null) {
            throw new IllegalArgumentException("文章ID、评论内容和用户ID不能为空");
        }
        
        Comments comment = new Comments();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        // 注意：gmtCreate 和 gmtModified 有 @TableField(fill = FieldFill.INSERT) 注解，会自动填充，不需要手动设置

        if (parentId != null) {
            Comments parentComment = commentMapper.selectById(parentId);
            if (parentComment == null) {
                throw new IllegalArgumentException("父评论不存在: " + parentId);
            }
            comment.setParentCommentId(parentId);
            // 设置根评论ID：如果父评论有根评论ID，使用父评论的根评论ID；否则使用父评论ID作为根评论ID
            comment.setRootCommentId(parentComment.getRootCommentId() != null ? parentComment.getRootCommentId() : parentId);
        }

        try {
            commentMapper.insert(comment);
        } catch (Exception e) {
            throw new RuntimeException("插入评论失败: " + e.getMessage(), e);
        }

        // 手动清除缓存（因为发表评论后，总评论数可能会发生变化）
        try {
            evictArticleDetailsCache(articleId);
            // 清除评论列表缓存
            if (cacheManager.getCache("comments::list::article") != null) {
                cacheManager.getCache("comments::list::article").evict(articleId);
            }
        } catch (Exception e) {
            // 缓存清除失败不影响评论插入的成功，只记录日志
            System.err.println("清除缓存失败: " + e.getMessage());
        }

        return comment;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        // 验证参数
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }
        
        // 查询评论是否存在且属于当前用户
        Comments comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在: " + commentId);
        }
        
        // 检查是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("无权删除他人的评论");
        }
        
        // 使用MyBatis-Plus的逻辑删除（@TableLogic会自动处理）
        try {
            commentMapper.deleteById(commentId);
        } catch (Exception e) {
            throw new RuntimeException("删除评论失败: " + e.getMessage(), e);
        }
        
        // 清除缓存
        try {
            evictArticleDetailsCache(comment.getArticleId());
            // 清除评论列表缓存
            if (cacheManager.getCache("comments::list::article") != null) {
                cacheManager.getCache("comments::list::article").evict(comment.getArticleId());
            }
        } catch (Exception e) {
            // 缓存清除失败不影响删除操作的成功，只记录日志
            System.err.println("清除缓存失败: " + e.getMessage());
        }
        
        return true;
    }

    private void evictArticleDetailsCache(Integer articleId) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article != null && article.getSlug() != null) {
                // 使用 CacheManager 获取名为 "articles::detail" 的缓存，并根据 slug 清除条目
                var cache = cacheManager.getCache("articles::detail");
                if (cache != null) {
                    cache.evict(article.getSlug());
                }
            }
        } catch (Exception e) {
            // 缓存清除失败不影响主流程，只记录日志
            System.err.println("清除文章详情缓存失败: " + e.getMessage());
        }
    }
}
