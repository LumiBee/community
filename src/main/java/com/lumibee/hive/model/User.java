package com.lumibee.hive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String accountId;
    private String name;
    private String token;
    private long gmtCreate;
    private long gmtModified;
    private String bio;
    private String avatarUrl;
    private String email;
    private String password;
}
