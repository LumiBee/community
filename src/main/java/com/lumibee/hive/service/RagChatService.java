package com.lumibee.hive.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private final String SYSTEM_PROMPT = """
            你是一个专业的知识库助手。请严格遵守以下规则：
            1. 只能根据提供的【参考上下文】来回答用户问题。
            2. 如果【参考上下文】中没有相关信息，请直接回答"知识库中未找到相关内容"，不要自行编造。
            3. 回答要条理清晰，可适当使用 Markdown 格式。

            【参考上下文】：
            {context}
            """;

    public RagChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public Flux<String> streamAskWithKnowledgeBase(String question, String fileName) {
        // 构建高级搜索请求
        SearchRequest.Builder requestBuilder = SearchRequest.builder()
                .query(question)
                .topK(5);

        // 动态添加元数据过滤
        if (fileName != null && !fileName.isEmpty()) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            requestBuilder.filterExpression(b.eq("source_filename", fileName).build());
        }

        SearchRequest searchRequest = requestBuilder.build();

        // 执行 ES 检索
        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        // 组装上下文
        String contextStr = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // 构建 Prompt
        Message systemMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("context", contextStr));
        UserMessage userMessage = new UserMessage(question);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // 发起对话
        return chatClient.prompt(prompt).stream().content();
    }

}
