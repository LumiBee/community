-- 添加allow_comments字段到articles表
-- 修复实体类与数据库表结构不匹配的问题
-- 生产环境安全：检查字段是否存在，避免重复添加

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE table_schema = DATABASE() 
     AND table_name = 'articles' 
     AND column_name = 'allow_comments') = 0,
    'ALTER TABLE `articles` ADD COLUMN `allow_comments` int DEFAULT 1 COMMENT ''是否允许评论，1允许，0不允许'' AFTER `likes`',
    'SELECT "Column allow_comments already exists"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
