CREATE TABLE comments (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论唯一ID',
                          article_id INT NOT NULL COMMENT '评论所属文章的ID',
                          user_id BIGINT NOT NULL COMMENT '发表评论的用户的ID',
                          content TEXT NOT NULL COMMENT '评论的具体内容',
                          parent_comment_id BIGINT NULL COMMENT '回复的父评论ID，顶级评论则为NULL',
                          root_comment_id BIGINT NULL COMMENT '该评论所属的顶级评论ID',
                          reply_to_user_id BIGINT NULL COMMENT '被回复用户的ID',
                          likes_count INT DEFAULT 0 COMMENT '评论的点赞数量',
                          gmt_create DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论创建时间',
                          gmt_modified DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '评论最后修改时间',
                          deleted int DEFAULT 0 COMMENT '逻辑删除标志，0表示未删除，1表示已删除',
                          version INT DEFAULT 0 COMMENT '乐观锁版本号，用于并发控制',

                          INDEX idx_article_id (article_id),
                          INDEX idx_user_id (user_id),
                          INDEX idx_parent_comment_id (parent_comment_id),
                          INDEX idx_root_comment_id (root_comment_id), -- 如果使用了 root_comment_id 字段，建议添加索引

                          CONSTRAINT fk_comment_article FOREIGN KEY (article_id) REFERENCES articles(article_id) ON DELETE CASCADE,
                          CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                          CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE SET NULL,
                          CONSTRAINT fk_comment_reply_to_user FOREIGN KEY (reply_to_user_id) REFERENCES user(id) ON DELETE SET NULL
) COMMENT '文章评论表';