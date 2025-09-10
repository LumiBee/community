package com.lumibee.hive.service;

import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.mapper.UserFollowingMapper;
import com.lumibee.hive.model.User;

import java.security.Principal;
import java.util.List;

public interface UserService {
    User selectByName(String name);
    User selectByEmail(String email);
    User selectByGithubId(String githubId);
    boolean updatePassword(Long id, String newPassword);
    int insert(User user);
    int updateById(User user);
    User selectById(Long id);
    List<User> selectByIds(List<Long> ids);
    User getCurrentUserFromPrincipal(Principal principal);
    boolean isFollowing(Long userId, Long followerId);
    boolean toggleFollow(Long userId, Long followerId);
    Integer countFansByUserId(Long id);
    Integer countFollowingByUserId(Long id);
    void refreshUserPrincipal(User user);
    boolean isFavoritedByCurrentUser(Long id, Integer articleId);
    
    // 获取UserMapper实例（用于调试）
    UserMapper getUserMapper();
    
    // 获取UserFollowingMapper实例（用于调试）
    UserFollowingMapper getUserFollowingMapper();

    User updateProfile(Long userId, String userName, String email, String bio);
}
