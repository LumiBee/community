package com.lumibee.hive.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleExcerptDTO {
    private Integer articleId;
    private String title;
    private String slug;
    private String excerpt;
    private LocalDateTime gmtModified;

    private Integer viewCount;
    private Integer likes;

    private Long userId;
    private String userName;
    private String avatarUrl;

    private String backgroundUrl;

}
