package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tags")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Integer tagId;
    private String name;
    private String slug;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    private Integer articleCount;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
    @Version
    private Integer version;
}
