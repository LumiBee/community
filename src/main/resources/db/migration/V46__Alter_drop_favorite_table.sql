-- 步骤 1: 先删除外键约束
-- 注意：'favorites_ibfk_1' 是你建表时定义的外键名称
ALTER TABLE `favorites` DROP FOREIGN KEY `favorites_ibfk_1`;

-- 步骤 2: 删除之前的唯一索引
-- 现在没有外键依赖它了，可以安全删除
ALTER TABLE `favorites` DROP INDEX `user_id`;

-- 步骤 3: 为 user_id 创建一个新的、非唯一的索引
-- 这是为了让外键能够重新建立
ALTER TABLE `favorites` ADD INDEX `idx_user_id` (`user_id`);

-- 步骤 4: 重新创建外键约束，它会使用上一步创建的新索引
-- 我们还用回原来的外键名和规则
ALTER TABLE `favorites` ADD CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;