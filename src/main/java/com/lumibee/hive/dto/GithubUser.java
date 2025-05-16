package com.lumibee.hive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GithubUser {
    private String name;
    private long id;
    private String bio;
    private String avatar_url;


}
