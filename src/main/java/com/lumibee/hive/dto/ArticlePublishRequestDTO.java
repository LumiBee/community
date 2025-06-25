package com.lumibee.hive.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticlePublishRequestDTO {
    private Integer articleId;
    private String title;
    private String content;
    private String excerpt;

    private List<String> tagsName;
    private String portfolioName;
}
