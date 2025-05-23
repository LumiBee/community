CREATE table tags (
    tag_id int NOT NULL primary key,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    gmt_create datetime NOT NULL,
    article_count int not null default 0
);

CREATE table article_tags (
    article_id int NOT NULL,
    tag_id int NOT NULL,
    primary key (article_id, tag_id),
    foreign key (article_id) references articles(article_id) on delete cascade,
    foreign key (tag_id) references tags(tag_id) on delete cascade
);