package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Portfolio {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Long userId;
    private String slug;
    private String description;
    private String coverImgUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtModified;

    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private Integer articleCount;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
    @Version
    private Integer version;
}
