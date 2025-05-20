package com.lumibee.hive.service;

import com.lumibee.hive.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        User user = null;
        // 1. 尝试按邮箱查找
        if (usernameOrEmail.contains("@")) { // 一个简单的判断是否可能是邮箱
            System.out.println("Attempting to find user by email: " + usernameOrEmail);
            user = userService.selectByEmail(usernameOrEmail);
        }

        // 2. 如果按邮箱未找到，或者输入的不像邮箱，则尝试按用户名查找
        if (user == null) {
            System.out.println("Attempting to find user by name: " + usernameOrEmail);
            user = userService.selectByName(usernameOrEmail);
        }

        if (user == null) {
            throw new UsernameNotFoundException("用户 '" + usernameOrEmail + "' 未找到");
        }
        System.out.println(">>>> User found in DB: ID=" + user.getId() + ", Name=" + user.getName() + ", Email=" + user.getEmail());
        System.out.println(">>>> User's email to be used for UserDetails: [" + user.getEmail() + "]");
        System.out.println(">>>> User's password from DB for UserDetails: [" + user.getPassword() + "]");

        // 创建 Spring Security 的 UserDetails 对象
        // 第一个参数是 principal 的唯一标识，通常是用户名或邮箱，用于后续获取 Principal 对象。
        // 我们可以选择使用数据库中的 name 或 email 作为 Spring Security 的 "username"。
        // 这里使用 user.getEmail() 作为 UserDetails 的 username
        // 重要的是 user.getPassword() 必须是数据库中存储的加密密码。
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}