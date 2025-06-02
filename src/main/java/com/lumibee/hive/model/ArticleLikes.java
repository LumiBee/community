package com.lumibee.hive.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("article_likes")
public class ArticleLikes {
    @TableId(type = IdType.AUTO)
    private long id;
    private long userId;
    private int articleId;
}
