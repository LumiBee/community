create table user
(
    id int primary key auto_increment,
    account_id varchar(100),
    name varchar(50),
    token varchar(36),
    GMT_CREATE BIGINT,
    GMT_MODIFIED BIGINT
);