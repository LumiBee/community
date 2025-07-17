package com.lumibee.hive.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {

    private String indexPrefix = "";

    private String articleIndexName = "articles";

    // 获取索引名称，前缀为空时直接返回文章索引名称，否则返回前缀加文章索引名称
    public String getIndexName() {
        return indexPrefix.isEmpty() ? articleIndexName : indexPrefix + "-" + articleIndexName;
    }
}
