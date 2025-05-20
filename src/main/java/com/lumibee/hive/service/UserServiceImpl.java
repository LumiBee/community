package com.lumibee.hive.service;

import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    public User selectByToken(String token) {
        return userMapper.selectByToken(token);
    }

    public User selectByName(String name) {
        return userMapper.selectByName(name);
    }

    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    public User selectByGithubId(String githubId) {
        return userMapper.selectByGithubId(githubId);
    }

    @Transactional
    public boolean updatePassword(Long id, String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPassword(newPassword);
        user.setGmtModified(System.currentTimeMillis());

        int updatedRows = userMapper.updateById(user);

        return updatedRows > 0;
    }

    public int insert(User user) {
        return userMapper.insert(user);
    }

    public int updateById(User user) {
        return userMapper.updateById(user);
    }

    public User selectById(Long id) {
        return userMapper.selectById(id);
    }
}
