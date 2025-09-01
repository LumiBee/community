package com.lumibee.hive.model;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnore
    private String password;
    private String githubId;
    private String qqOpenId;
    private String backgroundImgUrl;
    private UserRole role;

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

    public enum UserRole {
        regular,
        vip,
        admin
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.deleted == null || this.deleted == 0;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
