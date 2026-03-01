package com.lumibee.hive.controller;

import com.lumibee.hive.service.KnowledgeBaseService;
import com.lumibee.hive.service.RagChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai/rag")
public class RagController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final RagChatService ragChatService;

    public RagController(KnowledgeBaseService knowledgeBaseService, RagChatService ragChatService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.ragChatService = ragChatService;
    }

    /**
     * 上传文档构建知识库
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            knowledgeBaseService.processAndStorePdf(file);
            return ResponseEntity.ok("文档【" + file.getOriginalFilename() + "】处理并向量化成功！");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("文档处理失败：" + e.getMessage());
        }
    }

    /**
     * 知识库流式问答接口 (SSE)
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam("question") String question,
            @RequestParam(value = "fileName", required = false) String fileName) {

        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout

        ragChatService.streamAskWithKnowledgeBase(question, fileName)
                .subscribe(
                        content -> {
                            try {
                                emitter.send(content);
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete);

        return emitter;
    }
}