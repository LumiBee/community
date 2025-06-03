package com.lumibee.hive.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumibee.hive.model.Follower;

@Mapper
public interface UserFollowingMapper extends BaseMapper<Follower> {
    @Select("SELECT count(*) from following WHERE user_id = #{userId} AND follower_id = #{followerId}")
    Integer isFollowing(@Param("userId") Long userId, @Param("followerId") Long followerId);
    @Delete("DELETE FROM following where user_id = #{userId} AND follower_id = #{followerId}")
    void unfollowUser(@Param("userId") Long userId, @Param("followerId") Long followerId);
    @Insert("INSERT INTO following (user_id, follower_id) VALUES (#{userId}, #{followerId})")
    void followUser(@Param("userId") Long userId, @Param("followerId") Long followerId);
    @Select("SELECT COUNT(*) FROM following WHERE follower_id = #{id}")
    Integer countFansByUserId(@Param("id") Long id);
    @Select("SELECT COUNT(*) FROM following WHERE user_id = #{id}")
    Integer countFollowingByUserId(@Param("id") Long id);
}
