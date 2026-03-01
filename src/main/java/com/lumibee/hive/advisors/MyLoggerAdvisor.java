package com.lumibee.hive.advisors;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Log4j2
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientRequest before(ChatClientRequest request) {
        log.info("AI Request: {}", request.prompt().getContents());
        return request;
    }

    private void observeAfter(ChatClientResponse advisedResponse) {
        log.info("AI Response: {}", advisedResponse.chatResponse().getResult().getOutput().getText());
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        request = this.before(request);
        ChatClientResponse response = chain.nextCall(request);
        this.observeAfter(response);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        request = this.before(request);
        Flux<ChatClientResponse> responses = chain.nextStream(request);

        Mono<List<ChatClientResponse>> collectedResponses = responses.collectList();

        Mono<List<ChatClientResponse>> loggedMono = collectedResponses.doOnSuccess(list -> {
            if (list == null || list.isEmpty()) {
                return;
            }
            ChatClientResponse finalResponse = list.get(list.size() - 1);
            this.observeAfter(finalResponse);
        });

        return loggedMono.flatMapMany(Flux::fromIterable);
    }
}
