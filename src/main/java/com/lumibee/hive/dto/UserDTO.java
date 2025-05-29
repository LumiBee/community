package com.lumibee.hive.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String avatarUrl;
    private LocalDateTime gmtCreate;
}
