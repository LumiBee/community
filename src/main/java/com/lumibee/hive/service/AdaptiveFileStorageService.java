package com.lumibee.hive.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 自适应文件存储服务
 * 根据环境自动选择本地存储或OSS存储
 */
@Service
@Primary
public class AdaptiveFileStorageService implements FileStorageService {

    @Autowired
    private Environment environment;

    @Autowired
    private FileStorageServiceImpl localFileStorageService;

    @Autowired(required = false)
    private OssFileStorageService ossFileStorageService;

    private boolean isProductionEnvironment() {
        return "prod".equals(environment.getActiveProfiles()[0]);
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        if (isProductionEnvironment() && ossFileStorageService != null) {
            // 生产环境使用OSS
            return ossFileStorageService.uploadFile(file, subDirectory);
        } else {
            // 开发环境使用本地存储
            return localFileStorageService.storeFile(file, subDirectory);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (isProductionEnvironment() && ossFileStorageService != null) {
            // 生产环境删除OSS文件
            return ossFileStorageService.deleteFile(filePath);
        } else {
            // 开发环境删除本地文件
            return localFileStorageService.deleteFile(filePath);
        }
    }

    @Override
    public String getFileUrl(String fileName, String subDirectory) {
        if (isProductionEnvironment() && ossFileStorageService != null) {
            // 生产环境返回OSS URL
            String objectKey = subDirectory == null ? fileName : subDirectory + "/" + fileName;
            return ossFileStorageService.domain + "/" + objectKey;
        } else {
            // 开发环境返回本地URL
            return localFileStorageService.getFileUrl(fileName, subDirectory);
        }
    }

    @Override
    public Path getStorageLocation() {
        return localFileStorageService.getStorageLocation();
    }

    @Override
    public String getBaseUrl() {
        if (isProductionEnvironment() && ossFileStorageService != null) {
            // 生产环境返回OSS域名
            return ossFileStorageService.domain + "/";
        } else {
            // 开发环境返回本地URL
            return localFileStorageService.getBaseUrl();
        }
    }
}
