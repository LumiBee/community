package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comments {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer articleId;
    private Long userId;
    private String content;
    private Long parentCommentId;
    private Long rootCommentId;
    private Integer likesCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtModified;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
    @Version
    private Integer version;
}
