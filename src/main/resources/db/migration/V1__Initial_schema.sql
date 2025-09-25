-- 数据库初始化脚本 - 基线版本
-- 基于所有现有表结构的最终状态

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `token` varchar(36) DEFAULT NULL,
  `bio` varchar(256) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '用户头像',
  `email` varchar(255) DEFAULT NULL COMMENT '用户邮箱',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `github_id` varchar(255) DEFAULT NULL,
  `qq_open_id` varchar(255) DEFAULT NULL,
  `background_img_url` varchar(255) DEFAULT NULL COMMENT '背景图片地址',
  `role` ENUM('regular', 'vip', 'admin') NOT NULL DEFAULT 'regular' COMMENT '用户身份',
  `version` int DEFAULT '1',
  `deleted` int DEFAULT '0',
  `gmt_create` date DEFAULT NULL,
  `gmt_modified` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建文章表
CREATE TABLE IF NOT EXISTS `articles` (
  `article_id` int NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `slug` varchar(255) UNIQUE NOT NULL,
  `content` mediumtext NOT NULL,
  `excerpt` text,
  `status` ENUM('draft', 'published', 'archived', 'pending_review') NOT NULL DEFAULT 'draft',
  `view_count` int DEFAULT 0,
  `likes` int DEFAULT 0,
  `portfolio_id` int DEFAULT NULL,
  `background_url` varchar(255) DEFAULT NULL,
  `is_featured` boolean DEFAULT false,
  `version` int DEFAULT '1',
  `deleted` int DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_gmt_create` (`gmt_create`),
  KEY `idx_portfolio_id` (`portfolio_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建标签表
CREATE TABLE IF NOT EXISTS `tags` (
  `tag_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL UNIQUE,
  `gmt_create` datetime NOT NULL,
  `article_count` int NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `deleted` boolean NOT NULL DEFAULT false,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建文章标签关联表
CREATE TABLE IF NOT EXISTS `article_tags` (
  `article_id` int NOT NULL,
  `tag_id` int NOT NULL,
  PRIMARY KEY (`article_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_article_tags_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_article_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`tag_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建作品集表
CREATE TABLE IF NOT EXISTS `portfolio` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL UNIQUE,
  `description` text DEFAULT NULL,
  `cover_img_url` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `version` int NOT NULL DEFAULT 0,
  `deleted` boolean NOT NULL DEFAULT false,
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建文章点赞表
CREATE TABLE IF NOT EXISTS `article_likes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `article_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`,`article_id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_article_likes_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_article_likes_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建评论表
CREATE TABLE IF NOT EXISTS `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论唯一ID',
  `article_id` int NOT NULL COMMENT '评论所属文章的ID',
  `user_id` bigint NOT NULL COMMENT '发表评论的用户的ID',
  `content` text NOT NULL COMMENT '评论的具体内容',
  `parent_comment_id` bigint DEFAULT NULL COMMENT '回复的父评论ID，顶级评论则为NULL',
  `root_comment_id` bigint DEFAULT NULL COMMENT '该评论所属的顶级评论ID',
  `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复用户的ID',
  `likes_count` int DEFAULT 0 COMMENT '评论的点赞数量',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评论创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '评论最后修改时间',
  `deleted` int DEFAULT 0 COMMENT '逻辑删除标志，0表示未删除，1表示已删除',
  `version` int DEFAULT 0 COMMENT '乐观锁版本号，用于并发控制',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_comment_id` (`parent_comment_id`),
  KEY `idx_root_comment_id` (`root_comment_id`),
  CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_comment_reply_to_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章评论表';

-- 创建评论点赞表
CREATE TABLE IF NOT EXISTS `comment_likes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞记录唯一ID',
  `comment_id` bigint NOT NULL COMMENT '被点赞的评论ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户的ID',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user_like` (`comment_id`,`user_id`) COMMENT '确保用户对同一评论只能点赞一次',
  KEY `idx_like_user_id` (`user_id`),
  CONSTRAINT `fk_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- 创建关注表
CREATE TABLE IF NOT EXISTS `following` (
  `user_id` bigint NOT NULL,
  `follower_id` bigint NOT NULL,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`follower_id`),
  KEY `idx_follower_id` (`follower_id`),
  CONSTRAINT `chk_cannot_follow_self` CHECK (`user_id` <> `follower_id`),
  CONSTRAINT `fk_following_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_following_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建收藏表
CREATE TABLE IF NOT EXISTS `favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `is_public` boolean NOT NULL DEFAULT false,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` int NOT NULL DEFAULT 0,
  `deleted` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建文章收藏关联表
CREATE TABLE IF NOT EXISTS `article_favorites` (
  `favorite_id` bigint NOT NULL,
  `article_id` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`favorite_id`,`article_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_article_favorites_favorite` FOREIGN KEY (`favorite_id`) REFERENCES `favorites` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_article_favorites_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加外键约束
ALTER TABLE `articles` ADD CONSTRAINT `fk_articles_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
ALTER TABLE `articles` ADD CONSTRAINT `fk_articles_portfolio` FOREIGN KEY (`portfolio_id`) REFERENCES `portfolio` (`id`);
ALTER TABLE `portfolio` ADD CONSTRAINT `fk_portfolio_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

-- 创建索引优化查询性能
CREATE INDEX `idx_articles_status` ON `articles` (`status`);
CREATE INDEX `idx_articles_is_featured` ON `articles` (`is_featured`);
CREATE INDEX `idx_comments_deleted` ON `comments` (`deleted`);
CREATE INDEX `idx_favorites_deleted` ON `favorites` (`deleted`);
CREATE INDEX `idx_portfolio_deleted` ON `portfolio` (`deleted`);
CREATE INDEX `idx_tags_deleted` ON `tags` (`deleted`);

-- 设置字符集和排序规则
ALTER DATABASE `community` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
