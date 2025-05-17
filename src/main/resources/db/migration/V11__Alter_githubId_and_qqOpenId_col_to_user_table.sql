ALTER table `user`
drop column account_id,
add column github_id varchar(255) null,
add column qq_open_id varchar(255) null;