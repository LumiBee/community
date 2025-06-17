package com.lumibee.hive.service;

import com.lumibee.hive.dto.CommentDTO;
import com.lumibee.hive.dto.UserDTO;
import com.lumibee.hive.model.Comments;

import java.util.List;

public interface CommentService {
    // 获取指定文章的评论列表
    public List<CommentDTO> getCommentsByArticleId(Integer articleId);
    public Comments addComment(Integer articleId, String content, Long userId, Long parentId);
}
