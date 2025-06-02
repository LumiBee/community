CREATE TABLE following (
    user_id BIGINT NOT NULL,
    follower_id BIGINT NOT NULL,
    gmt_create DATETIME DEFAULT CURRENT_TIMESTAMP,

    primary key (user_id, follower_id),
    foreign key (user_id) references user(id) ON DELETE CASCADE,
    foreign key (follower_id) references user(id) ON DELETE CASCADE,
    INDEX idx_follower_id (follower_id),

    CONSTRAINT chk_cannot_follow_self CHECK(user_id <> follower_id)
)