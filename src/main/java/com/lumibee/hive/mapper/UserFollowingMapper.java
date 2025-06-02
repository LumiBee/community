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
    /**
     * 检查用户是否关注了另一个用户
     * @param userId 当前用户ID（关注者）
     * @param followerId 被关注的用户ID（作者）
     * @return 1 如果存在关注关系，0 如果不存在
     */
    @Select("SELECT count(*) from following WHERE user_id = #{userId} AND follower_id = #{followerId}")
    Integer isFollowing(@Param("userId") Long userId, @Param("followerId") Long followerId);
    
    /**
     * 取消关注
     * @param userId 当前用户ID（关注者）
     * @param followerId 被关注的用户ID（作者）
     */
    @Delete("DELETE FROM following where user_id = #{userId} AND follower_id = #{followerId}")
    void unfollowUser(@Param("userId") Long userId, @Param("followerId") Long followerId);
    
    /**
     * 添加关注
     * @param userId 当前用户ID（关注者）
     * @param followerId 被关注的用户ID（作者）
     */
    @Insert("INSERT INTO following (user_id, follower_id) VALUES (#{userId}, #{followerId})")
    void followUser(@Param("userId") Long userId, @Param("followerId") Long followerId);
}
