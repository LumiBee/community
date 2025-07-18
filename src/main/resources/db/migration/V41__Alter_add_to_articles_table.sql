ALTER table articles
add column background_url varchar(255) default null,
    add column is_featured boolean default false;