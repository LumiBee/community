package com.lumibee.hive.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel dashScopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashScopeChatModel);

        // 设置查询重写器
        queryTransformer = RewriteQueryTransformer.builder().
                chatClientBuilder(builder).
                build();
    }

    public String rewriteQuery(String prompt) {
        // 使用 QueryTransformer 来转换查询
        return queryTransformer.transform(new Query(prompt)).text();
    }

}
