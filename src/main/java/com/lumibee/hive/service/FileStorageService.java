package com.lumibee.hive.service;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {
    
    /**
     * 存储文件
     *
     * @param file 要存储的文件
     * @param subDirectory 子目录
     * @return 存储后的文件路径
     * @throws IOException 如果存储过程中发生IO错误
     */
    String storeFile(MultipartFile file, String subDirectory) throws IOException;
    
    /**
     * 删除文件
     *
     * @param filePath 要删除的文件路径
     * @return 是否成功删除
     */
    boolean deleteFile(String filePath);
    
    /**
     * 获取文件的完整访问URL
     *
     * @param fileName 文件名
     * @param subDirectory 子目录
     * @return 文件的完整访问URL
     */
    String getFileUrl(String fileName, String subDirectory);
    
    /**
     * 获取文件存储根目录
     *
     * @return 文件存储根目录
     */
    Path getStorageLocation();
} 