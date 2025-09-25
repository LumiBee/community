-- 添加allow_comments字段到articles表
-- 修复实体类与数据库表结构不匹配的问题

ALTER TABLE `articles` 
ADD COLUMN `allow_comments` int DEFAULT 1 COMMENT '是否允许评论，1允许，0不允许' AFTER `likes`;
