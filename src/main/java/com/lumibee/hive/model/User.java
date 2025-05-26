package com.lumibee.hive.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
public class User implements UserDetails, Principal {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private String token;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtModified;

    private String bio;
    private String avatarUrl;
    private String email;
    private String password;
    private String githubId;
    private String qqOpenId;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
    @Version
    private Integer version;

    public boolean isGithubOAuthUser() {
        return this.githubId != null;
    }
    public boolean isQQOpenAuthUser() {
        return this.qqOpenId != null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return this.name;
    }

}
