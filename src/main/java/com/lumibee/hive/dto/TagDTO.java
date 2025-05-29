package com.lumibee.hive.dto;

import lombok.Data;

@Data
public class TagDTO {
    private Integer tagId;
    private String name;
    private String slug;
    private Integer articleCount;

}
