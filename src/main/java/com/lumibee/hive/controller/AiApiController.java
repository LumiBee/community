package com.lumibee.hive.controller;

import com.lumibee.hive.service.OkHttpAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiApiController {

    @Autowired private OkHttpAiService okHttpAiService;

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
            return ResponseEntity.badRequest().body(new SummaryResponse("文本内容不能为空。"));
        }

        try {
            // 调用 DeepSeek 服务生成摘要
            String summary = okHttpAiService.generateSummary(request.getTextContent(), request.getMaxLength());
            return ResponseEntity.ok(new SummaryResponse(summary));
        } catch (Exception e) {
            // 处理异常并返回错误信息
            return ResponseEntity.status(500).body(new SummaryResponse("生成摘要失败，请稍后再试。"));
        }
    }
}