package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("articles")
public class Article implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer articleId;

    private Long userId;

    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private ArticleStatus status;
    private Integer portfolioId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtModified;

    @TableField(fill = FieldFill.INSERT)
    private Integer viewCount;
    private Integer likes;
    private Integer allowComments;

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String avatarUrl;
    @TableField(exist = false)
    private boolean isLiked;
    @TableField(exist = false)
    private boolean isFollowedByCurrentUser;
    @TableField(exist = false)
    private List<Tag> tags;
    @TableField(exist = false)
    private Portfolio portfolio;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
    @Version
    private Integer version;

    public enum ArticleStatus {
        draft,
        published,
        archived
    }

}
