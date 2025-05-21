package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private String token;

    @TableField(fill = FieldFill.INSERT)
    private LocalDate gmtCreate;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDate gmtModified;

    private String bio;
    private String avatarUrl;
    private String email;
    private String password;
    private String githubId;
    private String qqOpenId;

    @TableLogic
    private Integer deleted;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    public boolean isGithubOAuthUser() {
        return this.githubId != null;
    }
    public boolean isQQOpenAuthUser() {
        return this.qqOpenId != null;
    }
}
