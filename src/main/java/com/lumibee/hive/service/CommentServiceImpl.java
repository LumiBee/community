package com.lumibee.hive.service;

import com.lumibee.hive.dto.CommentDTO;
import com.lumibee.hive.dto.UserDTO;
import com.lumibee.hive.mapper.CommentMapper;
import com.lumibee.hive.model.Comments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired private CommentMapper commentMapper;

    @Override
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
        Comments comment = new Comments();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setGmtCreate(LocalDateTime.now());
        comment.setGmtModified(LocalDateTime.now());

        if (parentId != null) {
            Comments parentComment = commentMapper.selectById(parentId);
            if (parentComment != null) {
                comment.setParentCommentId(parentId);
                comment.setRootCommentId(parentComment.getRootCommentId() != null ? parentComment.getRootCommentId() : parentId);
            }
        }

        commentMapper.insert(comment);
        return comment;
    }
}
