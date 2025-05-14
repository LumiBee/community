package com.lumibee.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@org.apache.ibatis.annotations.Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT id, account_id, name, token, GMT_CREATE, GMT_MODIFIED FROM user WHERE token = #{token}")
    User findByToken(@Param("token")String token);
}
