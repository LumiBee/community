-- V23__Alter_id_col_auto_to_article_table.sql

-- Step 1: Drop the existing foreign key constraint from 'article_tags' table.
-- The constraint name 'article_tags_ibfk_1' is taken from your error message.
ALTER TABLE `community`.`article_tags` DROP FOREIGN KEY `article_tags_ibfk_1`;

-- Step 2: Modify the 'article_id' column in the 'articles' table.
-- This assumes 'article_id' is already the primary key and you just want to add AUTO_INCREMENT.
-- If 'article_id' is NOT yet the primary key and you also need to make it so,
-- and you've ensured no other primary key exists (which was your previous "Multiple primary key defined" error),
-- you might need: ALTER TABLE `community`.`articles` MODIFY COLUMN `article_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY;
-- For now, assuming it's already PK and just needs AUTO_INCREMENT:
ALTER TABLE `community`.`articles` MODIFY COLUMN `article_id` INT NOT NULL AUTO_INCREMENT;

-- Step 3: Re-create the foreign key constraint on 'article_tags' table.
-- Make sure to use the same ON DELETE and ON UPDATE actions as the original constraint if they were specified.
-- You can check the original constraint DDL with `SHOW CREATE TABLE community.article_tags;`
ALTER TABLE `community`.`article_tags`
    ADD CONSTRAINT `article_tags_ibfk_1` -- Or a new name if you prefer, but using the old one is fine
        FOREIGN KEY (`article_id`) REFERENCES `community`.`articles`(`article_id`)
            ON DELETE RESTRICT -- Or CASCADE, SET NULL, etc., based on your original constraint or desired behavior
            ON UPDATE RESTRICT; -- Or CASCADE, etc.