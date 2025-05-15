package com.lumibee.hive.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiApiController {

    @Value("${deepseek.api.key}") // 从配置文件读取 DeepSeek API Key
    private String deepseekApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // Jackson ObjectMapper for JSON processing

    public AiApiController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // 请求体 DTO (与前端对应)
    public static class SummaryRequest {
        private String textContent;
        private int maxLength = 100;

        public String getTextContent() { return textContent; }
        public void setTextContent(String textContent) { this.textContent = textContent; }
        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    }

    // 响应体 DTO (与前端对应)
    public static class SummaryResponse {
        private String summary;
        public SummaryResponse(String summary) { this.summary = summary; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    @PostMapping("/generate-summary-deepseek") // 可以用一个更具体的路径
    public ResponseEntity<SummaryResponse> generateSummaryWithDeepSeek(@RequestBody SummaryRequest request) {
        if (request.getTextContent() == null || request.getTextContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new SummaryResponse("错误：输入文本不能为空"));
        }

        // DeepSeek API URL (请根据官方文档替换为正确的 URL)
        String deepseekApiUrl = "https://api.deepseek.com/chat/completions"; // 这是一个假设的 URL

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey); // 或者 headers.set("Authorization", "Bearer " + deepseekApiKey);

            // 构建 DeepSeek API 的请求体 (根据其文档调整)
            // 这是一个通用的 Chat Completion 格式示例，具体字段名和结构需参照 DeepSeek 文档
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "请为以下文本生成一个不多于150字的摘要，直接返回摘要内容，不要包含其他任何引导性文字或标签,摘要的目的是让读者能够快速了解文章的主要内容,摘要不能多于150字。如果内容过短，请直接返回原文。文本如下：\n\n" +
                    request.getTextContent());

            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat"); // 替换为 DeepSeek 提供的具体模型名称
            body.put("messages", Collections.singletonList(message));
            body.put("max_tokens", request.getMaxLength() + 100); // 允许一些余量给 token 计算
            body.put("temperature", 0.7); // 控制生成文本的随机性/创造性
            // body.put("stream", false); // 通常 для摘要，非流式响应更简单

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    deepseekApiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析 DeepSeek API 的响应 (根据其文档调整)
                // 假设 DeepSeek 返回的 JSON 结构与 OpenAI 类似，有一个 choices 数组
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);

                // 具体的路径需要看 DeepSeek API 的实际返回格式
                // 例如: responseBody.get("choices").get(0).get("message").get("content")
                String generatedSummary = "";
                if (responseBody.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> firstChoice = choices.get(0);
                        if (firstChoice.containsKey("message")) {
                            Map<String, Object> messageData = (Map<String, Object>) firstChoice.get("message");
                            if (messageData.containsKey("content")) {
                                generatedSummary = messageData.get("content").toString().trim();
                            }
                        }
                    }
                }

                if (generatedSummary.isEmpty()) {
                    // 如果解析失败或返回为空，可以尝试从其他可能的字段获取，或返回错误
                    System.err.println("DeepSeek API: Could not parse summary from response or summary is empty. Response: " + response.getBody());
                    return ResponseEntity.status(500).body(new SummaryResponse("错误：AI摘要生成失败 - 未能解析响应"));
                }

                // 简单后处理，移除可能的外部引号
                if (generatedSummary.startsWith("\"") && generatedSummary.endsWith("\"") && generatedSummary.length() > 2) {
                    generatedSummary = generatedSummary.substring(1, generatedSummary.length() - 1);
                }

                return ResponseEntity.ok(new SummaryResponse(generatedSummary));
            } else {
                System.err.println("DeepSeek API Error: " + response.getStatusCode() + " - " + response.getBody());
                return ResponseEntity.status(response.getStatusCode())
                        .body(new SummaryResponse("错误：AI摘要生成失败 - " + response.getStatusCode()));
            }

        } catch (Exception e) {
            System.err.println("Error calling DeepSeek API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new SummaryResponse("错误：AI摘要生成时发生内部错误"));
        }
    }
}