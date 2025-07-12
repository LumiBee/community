package com.lumibee.hive.service;

import com.lumibee.hive.model.ArticleDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ArticleRepository extends ElasticsearchRepository<ArticleDocument, Integer> {
    
    // 使用bool_prefix查询进行前缀匹配搜索
    @Query("""
        {
          "bool": {
            "should": [
              {
                "bool": {
                  "should": [
                    {
                      "match_bool_prefix": {
                        "title": {
                          "query": "?0",
                          "boost": 2.0
                        }
                      }
                    },
                    {
                      "match_bool_prefix": {
                        "content": {
                          "query": "?0",
                          "boost": 1.0
                        }
                      }
                    }
                  ]
                }
              }
            ],
            "minimum_should_match": 1
          }
        }
        """)
    List<ArticleDocument> findByTitleOrContentWithPrefix(String query);
}