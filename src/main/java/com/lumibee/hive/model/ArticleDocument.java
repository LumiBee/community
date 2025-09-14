package com.lumibee.hive.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "#{@elasticsearchProperties.indexName}")
@Data
public class ArticleDocument {
    @Id
    private Integer id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Text)
    private String excerpt;

    @Field(type = FieldType.Keyword)
    private String userName;

    @Field(type = FieldType.Keyword)
    private String avatarUrl;

    @Field(type = FieldType.Text)
    private String slug;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer likes;

    @Field(type = FieldType.Date)
    private String gmtModified;

    @Field(type = FieldType.Text)
    private String backgroundUrl;

    @Field(type = FieldType.Integer)
    private Long userId;
}
