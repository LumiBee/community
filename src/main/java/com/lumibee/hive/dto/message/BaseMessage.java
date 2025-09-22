package com.lumibee.hive.dto.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 基础消息类
 * 
 * 所有消息的基类，包含通用字段
 */
@Data
public abstract class BaseMessage {

    /**
     * 消息唯一ID
     */
    private String messageId;

    /**
     * 业务唯一ID（用于幂等性检查）
     */
    private String businessId;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 消息来源
     */
    private String source;

    /**
     * 消息版本
     */
    private String version = "1.0";

    public BaseMessage() {
        this.messageId = UUID.randomUUID().toString();
        this.createTime = LocalDateTime.now();
    }

    public BaseMessage(String businessId, String messageType, String source) {
        this();
        this.businessId = businessId;
        this.messageType = messageType;
        this.source = source;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }
}
