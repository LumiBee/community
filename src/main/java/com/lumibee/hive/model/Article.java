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
    private String userName;
    private String title;
    private String slug;
    private String portfolioName;
    private String content;
    private String excerpt;
    private String avatarUrl;
    private ArticleStatus status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;

    @TableField(fill = FieldFill.INSERT)
    private Integer viewCount;
    private Integer likes;
    private Integer allowComments;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private boolean isLiked;

    @TableField(exist = false)
    private boolean isFollowedByCurrentUser;

    @TableField(exist = false)
    private List<Tag> tags;

    @Version
    private Integer version;

    public enum ArticleStatus {
        draft,
        published,
        archived
    }

}
