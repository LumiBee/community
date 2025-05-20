package com.lumibee.hive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.entity.Article;
import org.apache.ibatis.annotations.Param;

public interface ArticleService {
    Page<Article> getHomepageArticle(long pageNum, long pageSize);
}
