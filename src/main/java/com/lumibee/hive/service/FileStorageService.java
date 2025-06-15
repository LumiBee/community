package com.lumibee.hive.service;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, String subDirectory) throws IOException;
    boolean deleteFile(String filePath);
    String getFileUrl(String fileName, String subDirectory);
    Path getStorageLocation();
    String getBaseUrl();
} 