package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@TableName("articles")
public class Article implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer articleId;

    private Long userId;
    private String userName;
    private String title;
    private String slug;
    private String filePath;
    private String excerpt;
    private String avatarUrl;
    private ArticleStatus status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDate gmtCreate;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDate gmtModified;

    private Integer viewCount;
    private Integer allowComments;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @Version
    private Integer version;

    public enum ArticleStatus {
        DRAFT,
        published,
        ARCHIVED,
        PENDING_REVIEW
    }

}
