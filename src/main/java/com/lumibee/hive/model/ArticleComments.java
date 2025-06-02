package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleComments {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
}
