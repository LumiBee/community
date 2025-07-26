package com.lumibee.hive.service;

import java.io.IOException;

import com.lumibee.hive.model.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

public interface ImgService {
    String uploadAvatar(@Param("userId") Long userId, @Param("avatarFile") MultipartFile avatarFile) throws IOException;
    String getAvatarUrl(Long userId);
    String uploadCover(@Param("userId") Long userId, @Param("coverImageFile") MultipartFile coverImageFile) throws IOException;
}