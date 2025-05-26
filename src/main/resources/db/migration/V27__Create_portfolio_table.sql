CREATE table portfolio (
    id int not null auto_increment primary key,
    name varchar(255) not null,
    slug varchar(255) not null unique,
    description varchar(255),
    gmt_create timestamp not null default current_timestamp,
    gmt_modified timestamp not null default current_timestamp on update current_timestamp
);

ALTER table articles
DROP column portfolio_name;

ALTER table articles
add column portfolio_id int,
add constraint fk_articles_portfolio
foreign key (portfolio_id) references portfolio(id);

