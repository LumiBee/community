CREATE TABLE user_checkin_stats (
  `user_id` bigint NOT NULL PRIMARY KEY,
  `total_days` int DEFAULT 0 COMMENT '累计签到天数',
  `last_checkin_date` date DEFAULT NULL COMMENT '上次签到日期',
  `current_days` int DEFAULT 0 COMMENT '当前连续签到天数',
  `history_days` int DEFAULT 0 COMMENT '历史连续签到天数',
  CONSTRAINT `fk_checkin_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);