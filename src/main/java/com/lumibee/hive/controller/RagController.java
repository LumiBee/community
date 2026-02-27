package com.lumibee.hive.controller;

import com.lumibee.hive.service.KnowledgeBaseService;
import com.lumibee.hive.service.RagChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

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
    public Flux<String> streamChat(
            @RequestParam("question") String question,
            @RequestParam(value = "fileName", required = false) String fileName) {

        return ragChatService.streamAskWithKnowledgeBase(question, fileName);
    }
}