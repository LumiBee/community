package com.lumibee.hive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article implements Serializable {
    private int articleId;
    private int userId;
    private String title;
    private String slug;
    private String filePath;
    private String excerpt;
    private String coverImageUrl;
    private ArticleStatus status;
    private ArticleVisibility visibility;
    private Date gmtCreate;
    private Date gmtModified;
    private Date gmtPublished;
    private int viewCount;
    private int allowComments;

    public enum ArticleStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED,
        PENDING_REVIEW
    }

    public enum ArticleVisibility {
        PUBLIC,
        PRIVATE,
        UNLISTED
    }
}
