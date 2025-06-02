CREATE TABLE comment_likes (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞记录唯一ID',
                               comment_id BIGINT NOT NULL COMMENT '被点赞的评论ID',
                               user_id BIGINT NOT NULL COMMENT '点赞用户的ID',
                               gmt_create DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',

                               UNIQUE KEY uk_comment_user_like (comment_id, user_id) COMMENT '确保用户对同一评论只能点赞一次',
                               INDEX idx_like_user_id (user_id), -- 如果需要查找某用户点赞过的所有评论

                               CONSTRAINT fk_like_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
                               CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '评论点赞表';