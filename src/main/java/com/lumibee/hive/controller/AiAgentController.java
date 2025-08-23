package com.lumibee.hive.controller;

import com.lumibee.hive.agent.BeeManus;
import com.lumibee.hive.agent.JavaApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiAgentController {

    @Resource
    private JavaApp javaApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping("/java_guider/chat/sync")
    public String doChatWithJavaGuider(String message, String conversationId) {
        return javaApp.doChatWithRag(message, conversationId);
    }

    @GetMapping("/java_guider/chat/async")
    public Flux<ServerSentEvent<String>> doChatWithJavaGuiderSSE(String message, String conversationId) {
        return javaApp.doChatWithStream(message, conversationId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/java_guider/chat/sse/emitter")
    public SseEmitter doChatWithSseEmitter(String message, String conversationId) {
        SseEmitter emitter = new SseEmitter(18000L); // 设置超时时间为 3 分钟
        // 获取 Flux 数据流并直接订阅
        javaApp.doChatWithStream(message, conversationId)
                .subscribe(
                        // 处理每个数据块
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 完成时调用
                        emitter::complete
                );
        return emitter;
    }

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        BeeManus beeManus = new BeeManus(allTools, dashscopeChatModel);
        return beeManus.runStream(message);
    }
}
