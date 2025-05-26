Alter table portfolio
add column version int not null default 0,
    add column deleted boolean not null default false;

Alter table tags
    add column version int not null default 0,
    add column deleted boolean not null default false;