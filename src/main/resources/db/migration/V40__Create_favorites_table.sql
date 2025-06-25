Create table favorites (
    id bigint not null auto_increment,
    name varchar(255) not null,
    user_id bigint not null,
    gmt_create DATETIME not null default current_timestamp,
    gmt_motified DATETIME not null default current_timestamp on update current_timestamp,
    description text default null,
    version int not null default 0,
    deleted int not null default 0,
    primary key (id),
    unique key (user_id),
    foreign key (user_id) references user(id) on delete cascade
);

CREATE table article_favorites (
   favorite_id bigint not null,
   article_id int not null,
   primary key (favorite_id, article_id),
   foreign key (favorite_id) references favorites(id) on delete cascade,
   foreign key (article_id) references articles(article_id) on delete cascade
)