package com.lumibee.hive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class Article implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer articleId;

    private Long userId;
    private String title;
    private String slug;
    private String filePath;
    private String excerpt;
    private String coverImageUrl;
    private ArticleStatus status;
    private ArticleVisibility visibility;

    @TableField(fill = FieldFill.INSERT)
    private LocalDate gmtCreate;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDate gmtModified;

    private Integer viewCount;
    private Integer allowComments;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;

    public enum ArticleStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED,
        PENDING_REVIEW
    }

    public enum ArticleVisibility {
        PUBLIC,
        PRIVATE,
        UNLISTED
    }
}
