package com.lumibee.hive.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    
    private final Path rootLocation;
    private final String baseUrl;
    
    public FileStorageServiceImpl(
            @Value("${file.upload.dir:./uploads}") String uploadDir,
            @Value("${file.base.url:/uploads/}") String baseUrl) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }
    
    @Override
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // 创建子目录
        Path targetLocation = this.rootLocation;
        if (StringUtils.hasText(subDirectory)) {
            targetLocation = targetLocation.resolve(subDirectory);
            Files.createDirectories(targetLocation);
        }
        
        // 生成唯一文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = extractExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + fileExtension;
        
        // 保存文件
        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = targetLocation.resolve(filename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回相对于存储根目录的路径
            String relativePath = subDirectory == null ? filename : subDirectory + "/" + filename;
            return relativePath;
        }
    }
    
    @Override
    public boolean deleteFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        
        try {
            Path path = this.rootLocation.resolve(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
    
    @Override
    public String getFileUrl(String fileName, String subDirectory) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        
        String relativePath = subDirectory == null ? fileName : subDirectory + "/" + fileName;
        return baseUrl + relativePath;
    }
    
    @Override
    public Path getStorageLocation() {
        return this.rootLocation;
    }
    
    private String extractExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "jpg"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
} 