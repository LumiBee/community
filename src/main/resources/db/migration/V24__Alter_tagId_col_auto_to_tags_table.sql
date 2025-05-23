SET foreign_key_checks = 0;

ALTER table community.tags
MODIFY column tag_id int not null auto_increment;

SET foreign_key_checks = 1;