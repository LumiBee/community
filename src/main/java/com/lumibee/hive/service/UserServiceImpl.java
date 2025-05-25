package com.lumibee.hive.service;

import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

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
        user.setGmtModified(LocalDateTime.now());

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

    @Override
    public User getCurrentUserFromPrincipal(Principal principal) {
        if (principal == null) return null;

        User currentUser = null;
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        }else {
            username = ((OAuth2User) principal).getAttribute("login");
        }

        if (username != null) {
            currentUser = userMapper.selectByName(username);
        }

        return currentUser;
    }
}
