package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE name = #{name} LIMIT 1")
    User selectByName(@Param("name") String name);
    @Select("SELECT * FROM user WHERE email = #{email} LIMIT 1")
    User selectByEmail(@Param("email") String email);
    @Select("SELECT * FROM user WHERE github_id = #{githubId} LIMIT 1")
    User selectByGithubId(@Param("githubId") String githubId);
    @Update("UPDATE user SET points = points + #{points} WHERE id = #{userId}")
    int changePoints(@Param("userId") Long userId, @Param("points") Integer points);
}
