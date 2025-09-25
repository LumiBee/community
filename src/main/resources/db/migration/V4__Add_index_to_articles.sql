
-- 为articles表添加复合索引以优化查询性能
-- 注意：MySQL不支持IF NOT EXISTS，所以先检查索引是否存在
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE table_schema = DATABASE() 
     AND table_name = 'articles' 
     AND index_name = 'idx_status_deleted_time') = 0,
    'CREATE INDEX `idx_status_deleted_time` ON `articles` (`status`, `deleted`, `gmt_modified`)',
    'SELECT "Index already exists"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;