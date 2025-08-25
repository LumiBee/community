package com.lumibee.hive.service;

import com.lumibee.hive.model.User;
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
        System.out.println("===== CustomUserServiceImpl.loadUserByUsername 被调用 =====");
        System.out.println("输入参数: " + usernameOrEmail);

        User user = null;
        // 1. 尝试按邮箱查找
        if (usernameOrEmail.contains("@")) { // 一个简单的判断是否可能是邮箱
            System.out.println("尝试按邮箱查找用户: " + usernameOrEmail);
            user = userService.selectByEmail(usernameOrEmail);
        }

        // 2. 如果按邮箱未找到，或者输入的不像邮箱，则尝试按用户名查找
        if (user == null) {
            System.out.println("尝试按用户名查找用户: " + usernameOrEmail);
            user = userService.selectByName(usernameOrEmail);
        }

        if (user == null) {
            System.out.println("未找到用户: " + usernameOrEmail);
            throw new UsernameNotFoundException("用户 '" + usernameOrEmail + "' 未找到");
        }
        
        System.out.println("找到用户: ID=" + user.getId() + ", Name=" + user.getName() + ", Email=" + user.getEmail());
        System.out.println("用户密码: " + (user.getPassword() != null ? "已设置" : "未设置"));
        
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            System.out.println("警告: 用户密码为空!");
            throw new UsernameNotFoundException("用户 '" + usernameOrEmail + "' 未设置密码，无法登录");
        }
        
        // 确保所有UserDetails接口方法都被正确实现
        System.out.println("检查UserDetails接口方法:");
        System.out.println("- getUsername(): " + user.getUsername());
        System.out.println("- getPassword(): " + (user.getPassword() != null ? "已设置" : "未设置"));
        System.out.println("- isAccountNonExpired(): " + user.isAccountNonExpired());
        System.out.println("- isAccountNonLocked(): " + user.isAccountNonLocked());
        System.out.println("- isCredentialsNonExpired(): " + user.isCredentialsNonExpired());
        System.out.println("- isEnabled(): " + user.isEnabled());
        
        return user;
    }
}