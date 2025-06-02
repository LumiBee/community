package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Follower {
    private Long userId;
    private Long followerId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
}
