CREATE TABLE user_checkin (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` bigint NOT NULL,
  `checkin_date` date NOT NULL,
  `points` int DEFAULT 0,
  UNIQUE KEY `uk_user_checkin` (`user_id`, `checkin_date`),
  CONSTRAINT `fk_checkin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);
