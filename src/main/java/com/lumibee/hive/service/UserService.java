package com.lumibee.hive.service;

import com.lumibee.hive.model.User;

public interface UserService {
    User selectByToken(String token);
    User selectByName(String name);
    User selectByEmail(String email);
    User selectByGithubId(String githubId);
    boolean updatePassword(Long id, String newPassword);
    int insert(User user);
    int updateById(User user);
    User selectById(Long id);

}
