-- 测试迁移文件：添加测试表
-- 这是一个简单的测试，验证Flyway工作正常

CREATE TABLE IF NOT EXISTS `test_migration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试迁移表';
