drop table articles;

ALTER TABLE user
    DROP PRIMARY KEY,
    MODIFY COLUMN id BIGINT NOT NULL,
    ADD PRIMARY KEY (id);

CREATE TABLE articles (
                          article_id INT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          title VARCHAR(255) NOT NULL,
                          slug VARCHAR(255) UNIQUE NOT NULL,
                          file_path VARCHAR(512) NOT NULL, -- 存储文章文件路径
                          excerpt TEXT,
                          cover_image_url VARCHAR(255),
                          status ENUM('draft', 'published', 'archived', 'pending_review') NOT NULL DEFAULT 'draft',
                          visibility ENUM('public', 'private', 'unlisted') NOT NULL DEFAULT 'public',
                          gmt_create DATETIME DEFAULT CURRENT_TIMESTAMP,
                          gmt_modified DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          gmt_published DATETIME,
                          view_count INT DEFAULT 0,
                          allow_comments BOOLEAN DEFAULT TRUE,
                          FOREIGN KEY (user_id) REFERENCES user(id)
);