package com.lumibee.hive.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DeepSeek API 相关的 DTO 类
 */
public class DeepSeekDto {
    
    @Data
    public static class DeepSeekRequest {
        private String model;
        private List<DeepSeekMessage> messages;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
    }

    @Data
    public static class DeepSeekMessage {
        private String role;
        private String content;
    }

    @Data
    public static class DeepSeekResponse {
        private List<DeepSeekChoice> choices;
    }

    @Data
    public static class DeepSeekChoice {
        private DeepSeekMessage message;
    }
}