create table article_likes (
    id bigint not null auto_increment primary key,
    user_id bigint not null,
    article_id int not null,
    constraint fk_user foreign key (user_id) references community.user(id),
    constraint fk_article foreign key (article_id) references community.articles(article_id),
    unique key (user_id, article_id)
)