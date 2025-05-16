package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@org.apache.ibatis.annotations.Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT id, account_id, name, token, GMT_CREATE, GMT_MODIFIED, avatar_url, bio, email, password FROM user WHERE token = #{token}")
    User selectByToken(@Param("token") String token);

    @Select("SELECT id, account_id, name, email , password FROM user WHERE name = #{name} LIMIT 1")
    User selectByName(@Param("name") String name);

    @Select("SELECT id, account_id, name, email, password FROM user WHERE email = #{email} LIMIT 1")
    User selectByEmail(@Param("email") String email);

}
