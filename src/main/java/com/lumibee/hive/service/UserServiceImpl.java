package com.lumibee.hive.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.lumibee.hive.mapper.ArticleFavoritesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    public UserMapper getUserMapper() {
        return userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public User selectByName(String name) {
        System.out.println("UserServiceImpl.selectByName: 直接从数据库加载用户: " + name);
        User user = userMapper.selectByName(name);
        if (user != null) {
            System.out.println("UserServiceImpl.selectByName: 找到用户: " + user.getId() + ", 密码: " + (user.getPassword() != null ? "已设置(长度=" + user.getPassword().length() + ")" : "未设置"));
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User selectByEmail(String email) {
        System.out.println("UserServiceImpl.selectByEmail: 直接从数据库加载用户: " + email);
        User user = userMapper.selectByEmail(email);
        if (user != null) {
            System.out.println("UserServiceImpl.selectByEmail: 找到用户: " + user.getId() + ", 密码: " + (user.getPassword() != null ? "已设置(长度=" + user.getPassword().length() + ")" : "未设置"));
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User selectByGithubId(String githubId) {
        System.out.println("UserServiceImpl.selectByGithubId: 直接从数据库加载用户: " + githubId);
        User user = userMapper.selectByGithubId(githubId);
        if (user != null) {
            System.out.println("UserServiceImpl.selectByGithubId: 找到用户: " + user.getId() + ", 密码: " + (user.getPassword() != null ? "已设置(长度=" + user.getPassword().length() + ")" : "未设置"));
        }
        return user;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "usersByEmail", allEntries = true)
    })
    @Transactional
    public boolean updatePassword(Long id, String newPassword) {
        // 先获取完整的用户信息
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            return false;
        }
        
        // 只更新密码和修改时间
        User user = new User();
        user.setId(id);
        user.setPassword(newPassword);
        user.setGmtModified(LocalDateTime.now());

        System.out.println("更新用户密码: ID=" + id + ", 密码长度=" + (newPassword != null ? newPassword.length() : 0));
        int updatedRows = userMapper.updateById(user);
        System.out.println("密码更新结果: " + (updatedRows > 0 ? "成功" : "失败"));

        return updatedRows > 0;
    }

    @Override
    @Cacheable(value = "isFollowing", key = "#userId + '-' + #followerId")
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
            System.out.println("isFavoritedByCurrentUser: 参数无效 - userId=" + id + ", articleId=" + articleId);
            return false;
        }

        System.out.println("isFavoritedByCurrentUser: 查询收藏状态 - userId=" + id + ", articleId=" + articleId);
        Integer result = articleFavoritesMapper.selectIfFavoriteExists(id, articleId);
        System.out.println("isFavoritedByCurrentUser: 查询结果=" + result);
        boolean isFavorited = result != null && result > 0;
        System.out.println("isFavoritedByCurrentUser: 最终结果=" + isFavorited);
        return isFavorited; // 返回 true 如果存在收藏关系
    }

    @Override
    @Transactional
    public int insert(User user) {
        return userMapper.insert(user);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "users", key = "#user.id"),
                    @CacheEvict(value = "users", key = "#user.name"),
                    @CacheEvict(value = "usersByEmail", key = "#user.email"),
                    @CacheEvict(value = "usersByGithubId", key = "#user.githubId", condition = "#user.githubId != null")
            }
    )
    @Transactional
    public int updateById(User user) {
        return userMapper.updateById(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User selectById(Long id) {
        System.out.println("UserServiceImpl.selectById: 直接从数据库加载用户: " + id);
        User user = userMapper.selectById(id);
        if (user != null) {
            System.out.println("UserServiceImpl.selectById: 找到用户: " + user.getId() + ", 密码: " + (user.getPassword() != null ? "已设置(长度=" + user.getPassword().length() + ")" : "未设置"));
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> selectByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return userMapper.selectByIds(ids);
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
                // 直接从数据库加载用户，绕过缓存
                currentUser = userMapper.selectByName(identifier);
                if (currentUser == null) {
                    System.out.println("UserService: User not found by name '" + identifier + "', trying by email.");
                    currentUser = userMapper.selectByEmail(identifier);
                }
                
                // 检查密码是否正确加载
                if (currentUser != null) {
                    System.out.println("UserService: 从数据库加载用户成功: ID=" + currentUser.getId() + 
                                      ", 密码: " + (currentUser.getPassword() != null ? 
                                                  "已设置(长度=" + currentUser.getPassword().length() + ")" : "未设置"));
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
                // 直接从数据库加载用户，绕过缓存
                currentUser = userMapper.selectByName(identifier);
                
                // 检查密码是否正确加载
                if (currentUser != null) {
                    System.out.println("UserService: 从数据库加载用户成功: ID=" + currentUser.getId() + 
                                      ", 密码: " + (currentUser.getPassword() != null ? 
                                                  "已设置(长度=" + currentUser.getPassword().length() + ")" : "未设置"));
                }
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
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "users", key = "#followerId"),
            @CacheEvict(value = "isFollowing", key = "#userId + '-' + #followerId"),
            @CacheEvict(value = "followingCount", key = "#userId"),
            @CacheEvict(value = "fansCount", key = "#followerId")
    })
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
    @Cacheable(value = "fansCount", key = "#id")
    @Transactional(readOnly = true)
    public Integer countFansByUserId(Long id) {
        return userFollowingMapper.countFansByUserId(id);
    }

    @Override
    @Cacheable(value = "followingCount", key = "#id")
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
