package com.lumibee.hive.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private Integer articleId;
    private Long userId;
    private String content;
    private Long parentCommentId;
    private Long rootCommentId;
    private Integer likesCount;
    private LocalDateTime gmtCreate;

    private String userName;
    private String avatarUrl;

    private List<CommentDTO> replies;
}
