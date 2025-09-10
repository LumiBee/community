package com.lumibee.hive.service;

import com.lumibee.hive.dto.DeepSeekDto.*;
import okhttp3.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class OkHttpAiService {

    @Autowired private OkHttpService okHttpService;

    @Value("${deepseek.api.key}")
    private String deepseekApiKey;

    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";

    public OkHttpAiService(OkHttpService okHttpService) {
        this.okHttpService = okHttpService;
    }

    // 同步生成摘要
    public String generateSummary(String textContent, int maxLength) {
        try {
            DeepSeekRequest request = createRequest(textContent, maxLength);
            
            // 创建包含API密钥的请求头
            Headers headers = new Headers.Builder()
                    .add("Authorization", "Bearer " + deepseekApiKey)
                    .add("Content-Type", "application/json")
                    .build();
            
            DeepSeekResponse response = okHttpService.post(
                    DEEPSEEK_API_URL,
                    request,
                    DeepSeekResponse.class,
                    headers
            );
            return extractSummary(response);
        } catch (Exception e) {
            return "生成摘要失败，请稍后再试。";
        }
    }

    private DeepSeekRequest createRequest(String textContent, int maxLength) {
        DeepSeekMessage message = new DeepSeekMessage();
        message.setRole("user");
        message.setContent("请为一下文本生成一个不多于 " + maxLength + " 字的摘要：\n" + textContent + "。输出的内容应该只包含摘要内容，而没有其他任何多余的信息！如果文本过短，请直接返回原文。");

        DeepSeekRequest request = new DeepSeekRequest();
        request.setModel("deepseek-chat");
        request.setMessages(Collections.singletonList(message));
        request.setMaxTokens(maxLength + 1000); // 预留一些空间
        request.setTemperature(0.7); // 可根据需要调整温度参数

        return request;
    }

    private String extractSummary(DeepSeekResponse response) {
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            DeepSeekChoice choice = response.getChoices().get(0);
            if (choice.getMessage() != null) {
                return choice.getMessage().getContent().trim();
            }
        }
        return "未能生成摘要，请稍后再试。";
    }
}
