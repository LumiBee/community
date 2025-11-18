package com.lumibee.hive.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.dto.UserDTO;
import com.lumibee.hive.model.Follower;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lumibee.hive.mapper.ArticleFavoritesMapper;
import com.lumibee.hive.mapper.UserFollowingMapper;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.User;

/**
 * 用户服务实现类
 * 负责用户相关的业务逻辑处理，包括用户认证、资料管理、关注关系等
 */
@Service
@Log4j2
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFollowingMapper userFollowingMapper;

    @Autowired
    private ArticleFavoritesMapper articleFavoritesMapper;

    @Autowired
    private RedisClearCacheService redisClearCacheService;

    @Autowired
    private RedisMonitoringService redisMonitoringService;

    @Autowired
    private RedisCounterService redisCounterService;

    /**
     * 根据用户名查找用户
     * @param name 用户名
     * @return 用户信息
     */
    @Override
    @Cacheable(value = "users::profile", key = "#name")
    @Transactional(readOnly = true)
    public User selectByName(String name) {
        return userMapper.selectByName(name);
    }

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息
     */
    @Override
    @Transactional(readOnly = true)
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    /**
     * 根据GitHub ID查找用户
     * @param githubId GitHub ID
     * @return 用户信息
     */
    @Override
    @Transactional(readOnly = true)
    public User selectByGithubId(String githubId) {
        return userMapper.selectByGithubId(githubId);
    }

    /**
     * 检查用户是否关注了另一个用户
     * @param userId 被关注的用户ID
     * @param followerId 关注者用户ID
     * @return 是否关注
     */
    @Override
    @Cacheable(value = "users::follow", key = "#userId + '::' + #followerId")
    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, Long followerId) {
        if (userId == null || followerId == null || userId.equals(followerId)) {
            return false;
        }
        Integer result = userFollowingMapper.isFollowing(followerId, userId);
        return result == 1;
    }

    /**
     * 获取用户的粉丝数量
     * @param id 用户ID
     * @return 粉丝数量
     */
    @Override
    @Cacheable(value = "users::count", key = "'fans::' + #id")
    @Transactional(readOnly = true)
    public Integer countFansByUserId(Long id) {
        if (!redisCounterService.existsUserFansCount(id)) {
            Integer count = userFollowingMapper.countFansByUserId(id);
            // 将数据库中的粉丝数写入Redis缓存
            redisCounterService.setUserFansCount(id, count);
        }

        return redisCounterService.getUserFansCount(id);
    }

    /**
     * 获取用户的关注数量
     * @param id 用户ID
     * @return 关注数量
     */
    @Override
    @Cacheable(value = "users::count", key = "'followers::' + #id")
    @Transactional(readOnly = true)
    public Integer countFollowingByUserId(Long id) {
        if (!redisCounterService.existsUserFollowCount(id)) {
            Integer count = userFollowingMapper.countFollowingByUserId(id);
            // 将数据库中的粉丝数写入Redis缓存
            redisCounterService.setUserFollowCount(id, count);
        }

        return redisCounterService.getUserFollowCount(id);
    }

    /**
     * 更新用户密码
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updatePassword(Long id, String newPassword) {
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            return false;
        }

        User user = new User();
        user.setId(id);
        user.setPassword(newPassword);
        user.setGmtModified(LocalDateTime.now());

        int updatedRows = userMapper.updateById(user);
        
        // 清除用户相关缓存
        if (updatedRows > 0) {
            try {
                redisClearCacheService.clearUserRelatedCaches(id, existingUser.getName());
            } catch (Exception e) {
                System.err.println("清除用户相关缓存时出错: " + e.getMessage());
            }
        }
        
        return updatedRows > 0;
    }

    @Override
    @Transactional
    public boolean toggleFollow(Long followerId, Long userId) {
        // 这里followerId是当前用户（关注者），userId是要关注的作者（被关注者）
        if (followerId == null || userId == null || followerId.equals(userId)) {
            // 用户不能关注自己，或者参数无效
            return false;
        }


        // 检查当前用户是否已经关注了作者
        // 在following表中：user_id=被关注者，follower_id=关注者
        boolean isCurrentlyFollowing = userFollowingMapper.isFollowing(userId, followerId) == 1;

        if (isCurrentlyFollowing) {
            // 如果已经关注，则取消关注
            userFollowingMapper.unfollowUser(userId, followerId);

            // 关注/粉丝计数器减少
            redisCounterService.decrementUserFollow(followerId);
            redisCounterService.decrementUserFans(userId);

            // 清除关注相关缓存
            try {
                redisClearCacheService.clearUserStatusCaches(userId);
                redisClearCacheService.clearUserStatusCaches(followerId);
                redisClearCacheService.clearUserFollowCaches(userId, followerId);
            } catch (Exception e) {
                System.err.println("清除关注相关缓存时出错: " + e.getMessage());
            }

            return false;
        } else {
            // 如果未关注，则添加关注
            userFollowingMapper.followUser(userId, followerId);

            // 关注/粉丝计数器增加
            redisCounterService.incrementUserFollow(followerId);
            redisCounterService.incrementUserFans(userId);

            // 清除关注相关缓存
            try {
                redisClearCacheService.clearUserStatusCaches(userId);
                redisClearCacheService.clearUserStatusCaches(followerId);
                redisClearCacheService.clearUserFollowCaches(userId, followerId);
            } catch (Exception e) {
                System.err.println("清除关注相关缓存时出错: " + e.getMessage());
            }

            return true;
        }
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String userName, String email, String bio) {
        // 先获取完整的用户信息
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 检查用户名是否已被其他用户使用
        if (!userName.equals(existingUser.getName())) {
            User userWithSameName = userMapper.selectByName(userName);
            if (userWithSameName != null && !userWithSameName.getId().equals(userId)) {
                throw new IllegalArgumentException("用户名已被使用");
            }
        }

        // 检查邮箱是否已被其他用户使用
        if (!email.equals(existingUser.getEmail())) {
            User userWithSameEmail = userMapper.selectByEmail(email);
            if (userWithSameEmail != null && !userWithSameEmail.getId().equals(userId)) {
                throw new IllegalArgumentException("邮箱已被使用");
            }
        }

        // 更新用户信息
        User user = new User();
        user.setId(userId);
        user.setName(userName);
        user.setEmail(email);
        user.setBio(bio);
        user.setGmtModified(LocalDateTime.now());

        int updatedRows = userMapper.updateById(user);

        // 清除相关缓存（包括多种查询方式）
        try {
            redisClearCacheService.clearUserRelatedCaches(userId, existingUser.getName());
        } catch (Exception e) {
            System.err.println("清除用户相关缓存时出错: " + e.getMessage());
        }

        if (updatedRows > 0) {
            // 返回更新后的完整用户信息
            return userMapper.selectById(userId);
        } else {
            throw new RuntimeException("更新用户资料失败");
        }
    }

    @Override
    @Transactional
    public int changePoints(Long userId, Integer changePoints, String reason) {
        // 1. 参数验证
        if (userId == null || changePoints == null) {
            log.warn("积分变更失败：用户ID或积分变化值为空");
            return 0;
        }

        if (changePoints == 0) {
            log.warn("积分变更失败：积分变化值不能为0");
            return 0;
        }

        // 2. 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("积分变更失败：用户不存在，用户ID: {}", userId);
            return 0;
        }

        // 3. 检查积分变化后的合理性
        int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        int newPoints = currentPoints + changePoints;

        if (newPoints < 0) {
            log.warn("积分变更失败：积分不足，当前积分: {}, 需要扣除: {}", currentPoints, Math.abs(changePoints));
            return 0;
        }

        // 4. 执行积分更新
        int result = userMapper.changePoints(userId, changePoints);

        if (result > 0) {
            // 5. 清除相关缓存
            try {
                redisClearCacheService.clearUserRelatedCaches(userId, user.getName());
            } catch (Exception e) {
                log.error("清除用户积分缓存失败: {}", e.getMessage());
            }

            // 6. 记录积分变化日志
            log.info("用户积分变更成功：用户ID={}, 变化={}, 新积分={}, 原因={}",
                    userId, changePoints, newPoints, reason);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findFans(Long userId, long pageNum, long pageSize) {
        Page<Follower> fansPage = new Page<>(pageNum, pageSize);

        // 查询所有该用户的粉丝
        QueryWrapper<Follower> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        Page<Follower> relationships = userFollowingMapper.selectPage(fansPage, wrapper);

        // 如果没有粉丝关系，则直接返回一个空的
        if (relationships == null || relationships.getRecords().isEmpty()) {
            return new Page<>();
        }

        // 提取所有粉丝的ID，避免N+1问题
        List<Long> fanIds = relationships.getRecords().stream()
                .map(Follower::getFollowerId)
                .collect(Collectors.toList());

        List<User> fans = userMapper.selectByIds(fanIds);

        List<UserDTO> userDTOList = fans.stream()
                .map(fan -> new UserDTO(fan.getName(), fan.getAvatarUrl(), fan.getId(), fan.getBio(), fan.getRole()))
                .collect(Collectors.toList());

        // 创建一个新的Page对象来包装UserDTO列表
        Page<UserDTO> result = new Page<>(relationships.getCurrent(), relationships.getSize(), relationships.getTotal());
        result.setRecords(userDTOList);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findFollowing(Long userId, long pageNum, long pageSize) {
        Page<Follower> followingPage = new Page<>(pageNum, pageSize);
        // 查询所有该用户的关注关系
        QueryWrapper<Follower> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId);
        Page<Follower> relationships = userFollowingMapper.selectPage(followingPage, wrapper);

        // 如果没有关注关系，则直接返回一个空的
        if (relationships == null || relationships.getRecords().isEmpty()) {
            return new Page<>();
        }

        // 提取所有关注的ID，避免N+1问题
        List<Long> followingIds = relationships.getRecords().stream()
                .map(Follower::getUserId)
                .collect(Collectors.toList());

        List<User> following = userMapper.selectByIds(followingIds);

        List<UserDTO> userDTOList = following.stream()
                .map(followingUser -> new UserDTO(followingUser.getName(), followingUser.getAvatarUrl(), followingUser.getId(), followingUser.getBio(), followingUser.getRole()))
                .collect(Collectors.toList());

        // 创建一个新的Page对象来包装UserDTO列表
        Page<UserDTO> result = new Page<>(relationships.getCurrent(), relationships.getSize(), relationships.getTotal());
        result.setRecords(userDTOList);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavoritedByCurrentUser(Long id, Integer articleId) {
        if (id == null || articleId == null) {
            // 如果用户ID或文章ID无效，直接返回false
            return false;
        }

        Integer result = articleFavoritesMapper.selectIfFavoriteExists(id, articleId);
        boolean isFavorited = result != null && result > 0;
        return isFavorited; // 返回 true 如果存在收藏关系
    }

    @Override
    @Transactional
    public int insert(User user) {
        int result = userMapper.insert(user);
        
        // 清除相关缓存（新用户创建后需要清除所有用户列表缓存）
        if (result > 0) {
            try {
                redisClearCacheService.clearUserRelatedCaches(user.getId(), user.getName());
            } catch (Exception e) {
                System.err.println("清除用户相关缓存时出错: " + e.getMessage());
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public User findOrCreateUser(String githubId, String login, String email, String avatarUrl) {
        // 1. 查找GitHub ID是否已存在
        User existingUser = userMapper.selectByGithubId(githubId);
        if (existingUser != null) {
            log.info("GitHub ID已存在，直接返回用户信息：{}", existingUser);
            return existingUser;
        }

        // 2. 查找邮箱是否已存在, 如果存在则进行链接并同步更新GitHub ID和头像（如果为空）
        if (email != null && !email.isEmpty()) {
            existingUser = userMapper.selectByEmail(email);
            if (existingUser != null) {
                log.info("找到邮箱匹配的用户，现链接用户信息：{}", existingUser);
                existingUser.setGithubId(githubId);
                if (StrUtil.isBlank(existingUser.getAvatarUrl())) {
                    existingUser.setAvatarUrl(avatarUrl);
                }
                updateById(existingUser);

                redisClearCacheService.clearUserRelatedCaches(existingUser.getId(), existingUser.getName());
                return existingUser;
            }
        }

        // 3. 如果GitHub ID和邮箱都不存在，则创建新用户
        log.info("GitHub ID和邮箱都不存在，创建新用户: githubId={}, login={}, email={}, avatarUrl={}",
                githubId, login, email, avatarUrl);
        User newUser = new User();
        newUser.setGithubId(githubId);
        newUser.setName(login);
        newUser.setEmail(email);
        newUser.setAvatarUrl(avatarUrl);
        newUser.setGmtCreate(LocalDateTime.now());
        newUser.setGmtModified(LocalDateTime.now());
        newUser.setRole(User.UserRole.regular);
        newUser.setPoints(0);
        insert(newUser);

        redisClearCacheService.clearUserRelatedCaches(newUser.getId(), newUser.getName());
        return newUser;
    }


    @Override
    @Transactional
    public int updateById(User user) {
        int result = userMapper.updateById(user);
        
        // 清除相关缓存（用户信息更新后需要清除相关缓存）
        if (result > 0) {
            try {
                redisClearCacheService.clearUserRelatedCaches(user.getId(), user.getName());
            } catch (Exception e) {
                System.err.println("清除用户相关缓存时出错: " + e.getMessage());
            }
        }
        
        return result;
    }

    @Override
    @Cacheable(value = "users::profile", key = "'id::' + #id")
    @Transactional(readOnly = true)
    public User selectById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
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
            return null;
        }


        User currentUser = null;
        String identifier = null;

        // 优先处理 principal 就是 Authentication 的情况
        Object actualPrincipal = principal;
        if (principal instanceof Authentication) {
            actualPrincipal = ((Authentication) principal).getPrincipal();
            if (actualPrincipal == null) {
                return null;
            }
        }

        if (actualPrincipal instanceof com.lumibee.hive.model.User) {
            return (com.lumibee.hive.model.User) actualPrincipal;
        } else if (actualPrincipal instanceof UserDetails) {
            identifier = ((UserDetails) actualPrincipal).getUsername();
            if (identifier != null) {
                // 直接从数据库加载用户，绕过缓存
                currentUser = userMapper.selectByName(identifier);
                if (currentUser == null) {
                    currentUser = userMapper.selectByEmail(identifier);
                }

            }
        } else if (actualPrincipal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) actualPrincipal;
            // 尝试从OAuth2User获取多种可能的标识符，按优先级
            identifier = oauth2User.getAttribute("login"); // GitHub 的用户名
            if (identifier != null) {
                currentUser = userMapper.selectByName(identifier);
            }

            if (currentUser == null) {
                identifier = oauth2User.getAttribute("email"); // 尝试邮箱
                if (identifier != null) {
                    currentUser = userMapper.selectByEmail(identifier);
                }
            }

            if (currentUser == null) {
                // 对于OAuth2，getName() 通常返回的是提供商给该用户的唯一ID (例如数字字符串)
                String oauthProviderId = oauth2User.getName();
                // 假设您有一个方法可以通过GitHub ID (或其他OAuth提供商的ID) 来查找用户
                currentUser = userMapper.selectByGithubId(oauthProviderId);
                if (currentUser == null) {
                }
            }
        } else {
            // 尝试使用 principal.getName() 作为最后的手段，但它不一定总是用户名
            identifier = principal.getName(); // java.security.Principal 的 getName()
            if(identifier != null){
                // 直接从数据库加载用户，绕过缓存
                currentUser = userMapper.selectByName(identifier);
            }
        }

        if (currentUser != null) {
        } else {
        }
        return currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        User user = null;
        // 1. 尝试按邮箱查找
        if (usernameOrEmail.contains("@")) { // 一个简单的判断是否可能是邮箱
            user = this.selectByEmail(usernameOrEmail);
        }

        // 2. 如果按邮箱未找到，或者输入的不像邮箱，则尝试按用户名查找
        if (user == null) {
            user = this.selectByName(usernameOrEmail);
        }

        if (user == null) {
            throw new UsernameNotFoundException("用户 '" + usernameOrEmail + "' 未找到");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new UsernameNotFoundException("用户 '" + usernameOrEmail + "' 未设置密码，无法登录");
        }

        return user;
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
