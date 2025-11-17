package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("following")
public class Follower {
    private Long userId;
    private Long followerId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
}
