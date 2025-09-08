package com.lumibee.hive.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.lumibee.hive.agent.BeeManus;
import com.lumibee.hive.agent.JavaApp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI agent 服务", description = "AI agent 相关的 API 接口")
public class AiAgentController {

    @Resource
    private JavaApp javaApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping("/java_guider/chat/sync")
    @Operation(summary = "Java指导同步对话", description = "与Java指导AI进行同步对话")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "对话成功")
    })
    public String doChatWithJavaGuider(
            @Parameter(description = "对话消息") String message, 
            @Parameter(description = "会话ID") String conversationId) {
        return javaApp.doChatWithRag(message, conversationId);
    }

    @GetMapping("/java_guider/chat/async")
    @Operation(summary = "Java指导异步对话", description = "与Java指导AI进行异步流式对话")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "对话成功")
    })
    public Flux<ServerSentEvent<String>> doChatWithJavaGuiderSSE(
            @Parameter(description = "对话消息") String message, 
            @Parameter(description = "会话ID") String conversationId) {
        return javaApp.doChatWithStream(message, conversationId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/java_guider/chat/sse/emitter")
    @Operation(summary = "Java指导SSE对话", description = "与Java指导AI进行SSE流式对话")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "对话成功")
    })
    public SseEmitter doChatWithSseEmitter(
            @Parameter(description = "对话消息") String message, 
            @Parameter(description = "会话ID") String conversationId) {
        SseEmitter emitter = new SseEmitter(300000L); // 设置超时时间为 5 分钟
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
    @Operation(summary = "Manus对话", description = "与Manus AI进行对话")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "对话成功")
    })
    public SseEmitter doChatWithManus(
            @Parameter(description = "对话消息") String message) {
        BeeManus beeManus = new BeeManus(allTools, dashscopeChatModel);
        return beeManus.runStream(message);
    }
}
