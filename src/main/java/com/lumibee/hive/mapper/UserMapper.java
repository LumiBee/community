package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@org.apache.ibatis.annotations.Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE token = #{token} LIMIT 1")
    User selectByToken(@Param("token") String token);

    @Select("SELECT * FROM user WHERE name = #{name} LIMIT 1")
    User selectByName(@Param("name") String name);

    @Select("SELECT * FROM user WHERE email = #{email} LIMIT 1")
    User selectByEmail(@Param("email") String email);

    @Select("SELECT * FROM user WHERE account_id = #{accountId} LIMIT 1")
    User selectByAccountId(@Param("accountId") String accountId);
}
