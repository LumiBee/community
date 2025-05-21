package com.lumibee.hive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.model.Article;

public interface ArticleService {
    Page<Article> getHomepageArticle(long pageNum, long pageSize);
}
