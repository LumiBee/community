package com.lumibee.hive.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DraftDTO {
    private Integer articleId;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private LocalDateTime gmtModified;
    private List<TagDTO> tags;
    private Integer portfolioId;
    private String backgroundUrl;
}
