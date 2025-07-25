package com.lumibee.hive.service;

import java.security.Principal;
import java.time.LocalDateTime;

import com.lumibee.hive.mapper.ArticleFavoritesMapper;
import com.lumibee.hive.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumibee.hive.mapper.UserFollowingMapper;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.User;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserMapper userMapper;
    @Autowired private UserFollowingMapper userFollowingMapper;
    @Autowired private ArticleFavoritesMapper articleFavoritesMapper;

    @Override
    @Transactional(readOnly = true)
    public User selectByName(String name) {
        return userMapper.selectByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User selectByGithubId(String githubId) {
        return userMapper.selectByGithubId(githubId);
    }

    @Override
    @Transactional
    public boolean updatePassword(Long id, String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPassword(newPassword);
        user.setGmtModified(LocalDateTime.now());

        int updatedRows = userMapper.updateById(user);

        return updatedRows > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, Long followerId) {
        if (userId == null || followerId == null || userId.equals(followerId)) {
            // 用户不能关注自己，或者参数无效
            return false;
        }
        
        // userId是当前用户ID，followerId是作者ID
        // 检查当前用户(userId)是否关注了作者(followerId)
        Integer result = userFollowingMapper.isFollowing(userId, followerId);
        return result == 1; // 返回 true 如果存在关注关系
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavoritedByCurrentUser(Long id, Integer articleId) {
        if (id == null || articleId == null) {
            // 如果用户ID或文章ID无效，直接返回false
            return false;
        }

        Integer result = articleFavoritesMapper.selectIfFavoriteExists(id, articleId);
        return result == 1; // 返回 true 如果存在收藏关系
    }

    @Override
    @Transactional
    public int insert(User user) {
        return userMapper.insert(user);
    }

    @Override
    @Transactional
    public int updateById(User user) {
        return userMapper.updateById(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User selectById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserFromPrincipal(Principal principal) {
        if (principal == null) {
            System.out.println("UserService: Received a NULL Principal.");
            return null;
        }

        System.out.println("UserService: Processing Principal of type: " + principal.getClass().getName());

        User currentUser = null;
        String identifier = null;

        // 优先处理 principal 就是 Authentication 的情况
        Object actualPrincipal = principal;
        if (principal instanceof Authentication) {
            actualPrincipal = ((Authentication) principal).getPrincipal();
            if (actualPrincipal == null) {
                System.out.println("UserService: Authentication.getPrincipal() returned NULL.");
                return null;
            }
            System.out.println("UserService: Extracted actual Principal from Authentication: " + actualPrincipal.getClass().getName());
        }

        if (actualPrincipal instanceof com.lumibee.hive.model.User) {
            System.out.println("UserService: Principal is already an instance of com.lumibee.hive.model.User.");
            return (com.lumibee.hive.model.User) actualPrincipal;
        } else if (actualPrincipal instanceof UserDetails) {
            System.out.println("UserService: Principal is an instance of UserDetails.");
            identifier = ((UserDetails) actualPrincipal).getUsername();
            System.out.println("UserService: Identifier from UserDetails (username): " + identifier);
            if (identifier != null) {
                currentUser = userMapper.selectByName(identifier);
                if (currentUser == null) {
                    System.out.println("UserService: User not found by name '" + identifier + "', trying by email.");
                    currentUser = userMapper.selectByEmail(identifier);
                }
            }
        } else if (actualPrincipal instanceof OAuth2User) {
            System.out.println("UserService: Principal is an instance of OAuth2User.");
            OAuth2User oauth2User = (OAuth2User) actualPrincipal;
            // 尝试从OAuth2User获取多种可能的标识符，按优先级
            identifier = oauth2User.getAttribute("login"); // GitHub 的用户名
            if (identifier != null) {
                System.out.println("UserService: Identifier from OAuth2User (login attribute): " + identifier);
                currentUser = userMapper.selectByName(identifier);
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
            // 尝试使用 principal.getName() 作为最后的手段，但它不一定总是用户名
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

    @Override
    @Transactional
    public boolean toggleFollow(Long userId, Long followerId) {
        // 这里userId是当前用户，followerId是要关注的作者
        if (userId == null || followerId == null || userId.equals(followerId)) {
            // 用户不能关注自己，或者参数无效
            return false;
        }
        
        // 检查当前用户是否已经关注了作者
        boolean isCurrentlyFollowing = userFollowingMapper.isFollowing(userId, followerId) == 1;

        if (isCurrentlyFollowing) {
            // 如果已经关注，则取消关注
            userFollowingMapper.unfollowUser(userId, followerId);
            return false;
        } else {
            // 如果未关注，则添加关注
            userFollowingMapper.followUser(userId, followerId);
            return true;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countFansByUserId(Long id) {
        return userFollowingMapper.countFansByUserId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countFollowingByUserId(Long id) {
        return userFollowingMapper.countFollowingByUserId(id);
    }

    @Override
    public void refreshUserPrincipal(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }


}
