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

    @Query("""
        {
          "bool": {
              "must": [
                {
                  "more_like_this": {
                    "fields": ["title", "content"],
                    "like": [
                      {
                        "_index": "#{@elasticsearchProperties.indexName}",
                        "_id": "?2"
                      }
                    ],
                    "min_term_freq": 1,
                    "max_query_terms": 12,
                    "min_doc_freq": 1
                  }
                }
              ],
              "must_not": [
                {
                  "term": {
                    "_id": "?2"
                  }
                }
              ]
            }
        }
        """)
    List<ArticleDocument> selectRelatedArticles(String title, String content, Integer articleId);
}