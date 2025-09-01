package com.lumibee.hive.dto;

import com.lumibee.hive.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String avatarUrl;
    private LocalDateTime gmtCreate;
    private User.UserRole role;

    public UserDTO(String name, String avatarUrl, Long id) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.id = id;
    }
}
