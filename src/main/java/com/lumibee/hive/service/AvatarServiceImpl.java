package com.lumibee.hive.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lumibee.hive.model.User;

@Service
public class AvatarServiceImpl implements AvatarService {
    
    private static final String AVATAR_DIRECTORY = "avatars";
    private static final String DEFAULT_AVATAR = "/img/default01.jpg";
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private UserService userService;
    
    @Value("${avatar.max.size:2097152}") // 默认2MB
    private long maxAvatarSize;
    
    @Value("${avatar.allowed.extensions:jpg,jpeg,png,gif}")
    private String allowedExtensions;
    
    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile avatarFile) throws IOException {
        // 验证参数
        if (userId == null || avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid user ID or avatar file");
        }
        
        // 检查用户是否存在
        User user = userService.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        // 检查文件大小
        if (avatarFile.getSize() > maxAvatarSize) {
            throw new IllegalArgumentException("Avatar file size exceeds maximum allowed size");
        }
        
        // 检查文件类型
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(avatarFile.getOriginalFilename()));
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!allowedExtensions.contains(fileExtension)) {
            throw new IllegalArgumentException("Only " + allowedExtensions + " files are allowed for avatars");
        }
        
        // 删除旧头像
        deleteUserAvatar(user);
        
        // 存储新头像
        String relativePath = fileStorageService.storeFile(avatarFile, AVATAR_DIRECTORY);
        String avatarUrl = fileStorageService.getFileUrl(relativePath, null);
        
        // 更新用户头像URL
        user.setAvatarUrl(avatarUrl);
        user.setGmtModified(LocalDateTime.now());
        userService.updateById(user);
        
        return avatarUrl;
    }
    
    @Override
    @Transactional
    public boolean deleteAvatar(Long userId) {
        if (userId == null) {
            return false;
        }
        
        User user = userService.selectById(userId);
        if (user == null) {
            return false;
        }
        
        return deleteUserAvatar(user);
    }
    
    @Override
    public String getAvatarUrl(Long userId) {
        if (userId == null) {
            return DEFAULT_AVATAR;
        }
        
        User user = userService.selectById(userId);
        if (user == null || !StringUtils.hasText(user.getAvatarUrl())) {
            return DEFAULT_AVATAR;
        }
        
        return user.getAvatarUrl();
    }
    
    /**
     * 删除用户的头像文件
     */
    private boolean deleteUserAvatar(User user) {
        if (user == null || !StringUtils.hasText(user.getAvatarUrl())) {
            return false;
        }
        
        // 只删除自定义上传的头像，不删除默认头像或第三方OAuth头像
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl.startsWith(fileStorageService.getFileUrl("", null))) {
            // 提取相对路径
            String relativePath = avatarUrl.substring(fileStorageService.getFileUrl("", null).length());
            boolean deleted = fileStorageService.deleteFile(relativePath);
            
            if (deleted) {
                // 重置用户头像为默认
                user.setAvatarUrl(null);
                user.setGmtModified(LocalDateTime.now());
                userService.updateById(user);
            }
            
            return deleted;
        }
        
        return false;
    }
} 