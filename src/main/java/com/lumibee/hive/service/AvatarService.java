package com.lumibee.hive.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

/**
 * 头像处理服务接口
 */
public interface AvatarService {
    
    /**
     * 上传用户头像
     *
     * @param userId 用户ID
     * @param avatarFile 头像文件
     * @return 头像URL
     * @throws IOException 如果处理过程中发生IO错误
     */
    String uploadAvatar(Long userId, MultipartFile avatarFile) throws IOException;
    
    /**
     * 删除用户头像
     *
     * @param userId 用户ID
     * @return 是否成功删除
     */
    boolean deleteAvatar(Long userId);
    
    /**
     * 获取用户头像URL
     *
     * @param userId 用户ID
     * @return 头像URL，如果没有则返回默认头像
     */
    String getAvatarUrl(Long userId);
} 