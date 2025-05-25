package com.lumibee.hive.service;

import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
        if (principal == null) {
            System.out.println("UserService: Received a NULL Principal.");
            return null;
        }

        System.out.println("UserService: Processing Principal of type: " + principal.getClass().getName());

        User currentUser = null;
        String identifier = null; // 使用一个更通用的名字，如标识符

        // 优先处理 principal 就是 Authentication 的情况 (这在Controller中手动获取时很常见)
        Object actualPrincipal = principal;
        if (principal instanceof Authentication) {
            actualPrincipal = ((Authentication) principal).getPrincipal();
            if (actualPrincipal == null) {
                System.out.println("UserService: Authentication.getPrincipal() returned NULL.");
                return null;
            }
            System.out.println("UserService: Extracted actual Principal from Authentication: " + actualPrincipal.getClass().getName());
        }

        // 现在 actualPrincipal 是 UserDetails, OAuth2User, 或者您自定义的User类型等
        if (actualPrincipal instanceof com.lumibee.hive.model.User) {
            // 如果 Principal 本身就是您系统中的 User 对象实例
            // (例如，您的 CustomUserDetailsService 返回的就是 User，并且 User 实现了 UserDetails)
            // 或者在 OAuth2 流程中，您已经将其转换并设置为了 User 类型的 Principal
            System.out.println("UserService: Principal is already an instance of com.lumibee.hive.model.User.");
            return (com.lumibee.hive.model.User) actualPrincipal;
        } else if (actualPrincipal instanceof UserDetails) {
            System.out.println("UserService: Principal is an instance of UserDetails.");
            identifier = ((UserDetails) actualPrincipal).getUsername();
            System.out.println("UserService: Identifier from UserDetails (username): " + identifier);
            if (identifier != null) {
                // 假设 UserDetails.getUsername() 返回的是您 User 表中的 name 字段
                currentUser = userMapper.selectByName(identifier);
                if (currentUser == null) {
                    // 有些系统可能用邮箱作为 UserDetails.getUsername() 的返回值
                    System.out.println("UserService: User not found by name '" + identifier + "', trying by email.");
                    currentUser = userMapper.selectByEmail(identifier);
                }
            }
        } else if (actualPrincipal instanceof OAuth2User) {
            System.out.println("UserService: Principal is an instance of OAuth2User.");
            OAuth2User oauth2User = (OAuth2User) actualPrincipal;
            // 尝试从OAuth2User获取多种可能的标识符，按优先级
            identifier = oauth2User.getAttribute("login"); // 例如 GitHub 的用户名
            if (identifier != null) {
                System.out.println("UserService: Identifier from OAuth2User (login attribute): " + identifier);
                // 假设您在本地User表中有一个字段存储了这个OAuth的'login'名，或者您的User.name就是这个
                currentUser = userMapper.selectByName(identifier); // 或者 selectByOAuthLogin(identifier)
            }

            if (currentUser == null) {
                identifier = oauth2User.getAttribute("email"); // 尝试邮箱
                if (identifier != null) {
                    System.out.println("UserService: Identifier from OAuth2User (email attribute): " + identifier);
                    currentUser = userMapper.selectByEmail(identifier);
                }
            }

            if (currentUser == null) {
                // 对于OAuth2，getName() 通常返回的是提供商给该用户的唯一ID (例如数字字符串)
                // 您在CustomOAuth2AuthenticationSuccessHandler中是用这个ID来查找或创建用户的
                String oauthProviderId = oauth2User.getName();
                System.out.println("UserService: Identifier from OAuth2User (oauth2User.getName()): " + oauthProviderId);
                // 假设您有一个方法可以通过GitHub ID (或其他OAuth提供商的ID) 来查找用户
                currentUser = userMapper.selectByGithubId(oauthProviderId);
                if (currentUser == null) {
                    System.out.println("UserService: User not found by OAuth Provider ID: " + oauthProviderId);
                }
            }
        } else {
            System.out.println("UserService: Principal is of an UNKNOWN type to resolve to User directly: " + actualPrincipal.getClass().getName());
            // 如果您有其他认证方式，可以在这里添加更多的 instanceof 判断
            // 或者尝试使用 principal.getName() 作为最后的手段，但它不一定总是用户名
            identifier = principal.getName(); // java.security.Principal 的 getName()
            if(identifier != null){
                System.out.println("UserService: Fallback: Identifier from principal.getName(): " + identifier);
                currentUser = userMapper.selectByName(identifier);
            }
        }

        if (currentUser != null) {
            System.out.println("UserService: User resolved: " + currentUser.getName());
        } else {
            System.out.println("UserService: Failed to resolve User from Principal.");
        }
        return currentUser;
    }
}
