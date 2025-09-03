package com.lumibee.hive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumibee.hive.service.OkHttpAiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI 服务", description = "AI 相关的 API 接口，如文章摘要生成等")
public class AiApiController {

    @Autowired private OkHttpAiService okHttpAiService;

    // 请求体 DTO (与前端对应)
    public static class SummaryRequest {
        private String textContent;
        private int maxLength = 100;

        // 无参构造函数，确保JSON反序列化正常
        public SummaryRequest() {}
        
        public String getTextContent() { return textContent; }
        public void setTextContent(String textContent) { this.textContent = textContent; }
        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
        
        @Override
        public String toString() {
            return "SummaryRequest{textContent='" + textContent + "', maxLength=" + maxLength + "}";
        }
    }

    // 响应体 DTO (与前端对应)
    public static class SummaryResponse {
        private String summary;
        
        // 无参构造函数，确保JSON序列化正常
        public SummaryResponse() {}
        
        public SummaryResponse(String summary) { 
            this.summary = summary; 
        }
        
        public String getSummary() { 
            return summary; 
        }
        
        public void setSummary(String summary) { 
            this.summary = summary; 
        }
        
        @Override
        public String toString() {
            return "SummaryResponse{summary='" + summary + "'}";
        }
    }

    @PostMapping("/generate-summary-deepseek")
    @Operation(summary = "生成文章摘要", description = "使用 DeepSeek AI 模型为文章生成摘要")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "摘要生成成功", 
                    content = @Content(schema = @Schema(implementation = SummaryResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "AI 服务异常")
    })
    public ResponseEntity<SummaryResponse> generateSummaryWithDeepSeek(
        @Parameter(description = "摘要生成请求数据") @RequestBody SummaryRequest request) {
        System.out.println("=== AI摘要接口被调用 ===");
        System.out.println("收到AI摘要请求: " + request.getTextContent().substring(0, Math.min(50, request.getTextContent().length())) + "...");
        
        if (request.getTextContent() == null || request.getTextContent().trim().isEmpty()) {
            System.out.println("文本内容为空");
            return ResponseEntity.badRequest().body(new SummaryResponse("文本内容不能为空。"));
        }

        try {
            System.out.println("开始调用DeepSeek服务生成摘要...");
            // 调用 DeepSeek 服务生成摘要
            String summary = okHttpAiService.generateSummary(request.getTextContent(), request.getMaxLength());
            System.out.println("DeepSeek服务返回摘要: " + summary);
            
            // 检查摘要是否有效
            if (summary == null || summary.trim().isEmpty() || summary.contains("生成摘要失败")) {
                System.err.println("DeepSeek服务返回无效摘要: " + summary);
                return ResponseEntity.status(500).body(new SummaryResponse("DeepSeek服务返回无效摘要"));
            }
            
            SummaryResponse response = new SummaryResponse(summary);
            System.out.println("创建响应对象: " + response);
            System.out.println("响应对象的summary字段: " + response.getSummary());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 处理异常并返回错误信息
            System.err.println("生成摘要失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new SummaryResponse("生成摘要失败，请稍后再试。错误: " + e.getMessage()));
        }
    }
}