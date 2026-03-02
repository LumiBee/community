package com.lumibee.hive.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/java_guider/chat/sse/emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Java指导SSE对话", description = "与Java指导AI进行SSE流式对话")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "对话成功")
    })
    public SseEmitter doChatWithSseEmitter(
            @Parameter(description = "对话消息") String message,
            @Parameter(description = "会话ID") String conversationId) {
        return javaApp.doChatStream(message, conversationId);
    }

    @GetMapping(value = "/manus/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
