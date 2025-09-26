package com.lumibee.hive.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * 阿里云OSS文件存储服务
 * 仅在生产环境使用
 */
@Service
public class OssFileStorageService {

    @Autowired
    private OSS ossClient;

    @Autowired
    @Qualifier("ossBucketName")
    private String bucketName;

    @Autowired
    @Qualifier("ossDomain")
    public String domain;

    /**
     * 上传文件到OSS
     * @param file 文件
     * @param subDirectory 子目录
     * @return 文件访问URL
     * @throws IOException 上传异常
     */
    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 生成唯一文件名
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = extractExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + fileExtension;

        // 构建对象键（文件路径）
        String objectKey = subDirectory == null ? filename : subDirectory + "/" + filename;

        try (InputStream inputStream = file.getInputStream()) {
            // 设置对象元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 上传文件到OSS
            ossClient.putObject(bucketName, objectKey, inputStream, metadata);

            // 返回文件访问URL
            return domain + "/" + objectKey;
        }
    }

    /**
     * 获取文件访问URL
     * @param fileName 文件名
     * @param subDirectory 子目录
     * @return 文件访问URL
     */
    public String getFileUrl(String fileName, String subDirectory) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        
        String objectKey = subDirectory == null ? fileName : subDirectory + "/" + fileName;
        return domain + "/" + objectKey;
    }

    /**
     * 删除OSS中的文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }

        try {
            // 从URL中提取对象键
            String objectKey = extractObjectKeyFromUrl(fileUrl);
            if (objectKey == null) {
                return false;
            }

            // 删除对象
            ossClient.deleteObject(bucketName, objectKey);
            return true;
        } catch (Exception e) {
            System.err.println("删除OSS文件失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从URL中提取对象键
     * @param fileUrl 文件URL
     * @return 对象键
     */
    private String extractObjectKeyFromUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }

        // 移除域名部分，获取对象键
        if (fileUrl.startsWith(domain)) {
            return fileUrl.substring(domain.length() + 1); // +1 是为了移除开头的 "/"
        }

        return null;
    }

    /**
     * 提取文件扩展名
     * @param filename 文件名
     * @return 扩展名
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
