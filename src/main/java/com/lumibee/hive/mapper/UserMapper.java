package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT id, name, token, gmt_create, gmt_modified, bio, avatar_url, email, password, github_id, qq_open_id, background_img_url, deleted, version FROM user WHERE name = #{name} LIMIT 1")
    User selectByName(@Param("name") String name);
    @Select("SELECT id, name, token, gmt_create, gmt_modified, bio, avatar_url, email, password, github_id, qq_open_id, background_img_url, deleted, version FROM user WHERE email = #{email} LIMIT 1")
    User selectByEmail(@Param("email") String email);
    @Select("SELECT id, name, token, gmt_create, gmt_modified, bio, avatar_url, email, password, github_id, qq_open_id, background_img_url, deleted, version FROM user WHERE github_id = #{githubId} LIMIT 1")
    User selectByGithubId(@Param("githubId") String githubId);
}
