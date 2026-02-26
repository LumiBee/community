package com.lumibee.hive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.UserDTO;
import com.lumibee.hive.model.User;

import java.security.Principal;
import java.util.List;

public interface UserService {
    User selectByName(String name);

    User selectByEmail(String email);

    User selectByGithubId(String githubId);

    boolean updatePassword(long id, String newPassword);

    int insert(User user);

    User findOrCreateUser(String githubId, String login, String email, String avatarUrl);

    int updateById(User user);

    User selectById(long id);

    List<User> selectByIds(List<Long> ids);

    User getCurrentUserFromPrincipal(Principal principal);

    boolean isFollowing(long userId, long followerId);

    boolean toggleFollow(long userId, long followerId);

    int countFansByUserId(long id);

    int countFollowingByUserId(long id);

    void refreshUserPrincipal(User user);

    boolean isFavoritedByCurrentUser(long id, int articleId);

    User updateProfile(long userId, String userName, String email, String bio);

    int changePoints(long userId, int changePoints, String reason);

    Page<UserDTO> findFans(long userId, long pageNum, long pageSize);

    Page<UserDTO> findFollowing(long userId, long pageNum, long pageSize);

}
