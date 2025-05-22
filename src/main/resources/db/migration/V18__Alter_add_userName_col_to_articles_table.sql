Alter table community.articles
rename column cover_image_url to avatar_url ;

Alter table community.articles
add column user_name varchar(255);