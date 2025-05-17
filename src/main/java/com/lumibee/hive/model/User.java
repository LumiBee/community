package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private String token;
    private long gmtCreate;
    private long gmtModified;
    private String bio;
    private String avatarUrl;
    private String email;
    private String password;

    private String githubId;
    private String qqOpenId;

    public boolean isGithubOAuthUser() {
        return this.githubId != null;
    }
    public boolean isQQOpenAuthUser() {
        return this.qqOpenId != null;
    }
}
