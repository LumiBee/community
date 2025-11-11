package com.lumibee.hive.service;

import com.lumibee.hive.dto.CommentDTO;
import com.lumibee.hive.model.Comments;

import java.util.List;

public interface CommentService {
    // 获取指定文章的评论列表
    public List<CommentDTO> getCommentsByArticleId(Integer articleId);
    public Comments addComment(Integer articleId, String content, Long userId, Long parentId);
    // 删除评论（只能删除自己的评论）
    public boolean deleteComment(Long commentId, Long userId);
}
