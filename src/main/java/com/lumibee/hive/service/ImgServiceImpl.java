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
public class ImgServiceImpl implements ImgService {
    
    private static final String AVATAR_DIRECTORY = "avatars";
    private static final String BG_DIRECTORY = "backgrounds";
    private static final String DEFAULT_AVATAR = "/img/default01.jpg";
    
    @Autowired private FileStorageService fileStorageService;
    
    @Autowired private UserService userService;

    @Autowired private RedisCacheService cacheService;
    
    
    @Value("${avatar.max.size}") // 默认2MB
    private long maxAvatarSize;
    
    @Value("${avatar.allowed.extensions}")
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
        String fileExtension = extractExtension(originalFilename);
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
        
        // 自动清理相关缓存
        try {
            cacheService.clearUserArticleCaches(userId);
        } catch (Exception e) {
            System.err.println("ImgService: 清理缓存失败: " + e.getMessage());
            // 缓存清理失败不影响头像上传的成功
        }
        
        return avatarUrl;
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

    @Override
    public String uploadCover(Long userId, MultipartFile coverImageFile) throws IOException{
        // 调试信息
        
        // 验证参数
        if (userId == null || coverImageFile == null || coverImageFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid user ID or cover image file");
        }

        // 检查用户是否存在
        User user = userService.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        // 检查文件大小
        if (coverImageFile.getSize() > maxAvatarSize * 2) {
            throw new IllegalArgumentException("Cover image file size exceeds maximum allowed size");
        }

        // 检查文件类型
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(coverImageFile.getOriginalFilename()));
        String fileExtension = extractExtension(originalFilename);
        if (!allowedExtensions.contains(fileExtension)) {
            throw new IllegalArgumentException("Only " + allowedExtensions + " files are allowed for avatars");
        }

        // 删除旧封面
        deleteBackgroundImg(user);

        // 存储新背景
        String relativePath = fileStorageService.storeFile(coverImageFile, BG_DIRECTORY);
        String imgUrl = fileStorageService.getFileUrl(relativePath, null);

        // 更新背景URL
        user.setBackgroundImgUrl(imgUrl);
        user.setGmtModified(LocalDateTime.now());
        userService.updateById(user);
        userService.refreshUserPrincipal(user);

        return imgUrl;
    }


    private void deleteUserAvatar(User user) {
        if (user == null || !StringUtils.hasText(user.getAvatarUrl())) {
            return;
        }
        
        // 只删除自定义上传的头像，不删除默认头像或第三方OAuth头像
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return ;
        }

        String baseUrl = fileStorageService.getBaseUrl();
        if (baseUrl != null && avatarUrl.startsWith(baseUrl)) {
            // 提取相对路径
            String relativePath = avatarUrl.substring(baseUrl.length());
            boolean deleted = fileStorageService.deleteFile(relativePath);
            
            if (deleted) {
                // 重置用户头像为默认
                user.setAvatarUrl(null);
                user.setGmtModified(LocalDateTime.now());
                userService.updateById(user);
            }
        }
    }

    private void deleteBackgroundImg(User user) {
        if (user == null || !StringUtils.hasText(user.getBackgroundImgUrl())) {
            return;
        }

        // 只删除自定义上传的背景，不删除默认背景
        String backgroundImgUrl = user.getBackgroundImgUrl();
        if (backgroundImgUrl == null || backgroundImgUrl.isEmpty()) {
            return;
        }

        String baseUrl = fileStorageService.getBaseUrl();
        if (baseUrl != null && backgroundImgUrl.startsWith(baseUrl)) {
            // 提取相对路径
            String relativePath = backgroundImgUrl.substring(baseUrl.length());
            boolean deleted = fileStorageService.deleteFile(relativePath);

            if (deleted) {
                // 重置用户背景为默认
                user.setBackgroundImgUrl(null);
                user.setGmtModified(LocalDateTime.now());
                userService.updateById(user);
            }
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
} 