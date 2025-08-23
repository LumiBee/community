package com.lumibee.hive.agent;

import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.lumibee.hive.advisors.MyLoggerAdvisor;
import com.lumibee.hive.rag.QueryRewriter;
import com.lumibee.hive.utils.TransApi;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Log4j2
public class JavaApp {

   private final ChatClient chatClient;
   private final PromptTemplate javaPromptTemplate;
   private final MessageWindowChatMemory messageWindowChatMemory;
   private final QueryRewriter queryRewriter;


   @Value("${baidu.translate.app-id}")
   private String APP_ID;
   @Value("${baidu.translate.security-key}")
   private String SECURITY_KEY;

   @jakarta.annotation.Resource
   private ToolCallback[] allTools;


   /**
    * LoveApp的构造函数，通过Spring进行依赖注入。
    * 所有的依赖都通过构造函数参数传入，确保 final 字段被正确初始化。
    */
   public JavaApp(ChatClient.Builder builder,
                  //MysqlChatMemoryRepository mysqlChatMemoryRepository,
                  QueryRewriter queryRewriter,
                  @Value("classpath:/prompts/java-guider.st") Resource javaPromptResource) {

       this.javaPromptTemplate = new PromptTemplate(javaPromptResource);
       this.queryRewriter = queryRewriter;

       this.messageWindowChatMemory = MessageWindowChatMemory.builder()
               //.chatMemoryRepository(mysqlChatMemoryRepository)
               .maxMessages(100) // 直接在这里使用魔法值，或者定义为常量
               .build();

       this.chatClient = builder
               .defaultAdvisors(new MyLoggerAdvisor())
               .build();
   }

   public String doChatWithRag(String message, String conversationId) {
       log.info("LoveApp.doChatWithRag: message={}, conversationId={}", message, conversationId);
       // 使用 QueryRewriter 来重写和翻译查询, 获取翻译后的消息
       String apiResponse = new TransApi(APP_ID, SECURITY_KEY)
               .getTransResult(queryRewriter.rewriteQuery(message), "auto", "zh");

       // 解析API响应，提取翻译后的消息
       String translatedMessage = new JSONObject(apiResponse)
               .getJSONArray("trans_result")
               .getJSONObject(0)
               .getString("dst");

       log.info("Translated message: {}", translatedMessage);

       return chatClient.prompt()
               .user(translatedMessage)
               .advisors(a -> a.param("conversationId", conversationId))
               .call()
               .content();
   }


   public Flux<String> doChatWithStream(String message, String conversationId) {
       log.info("JavaApp.doChatWithStream: message={}, conversationId={}", message, conversationId);
       return chatClient.prompt(javaPromptTemplate.create())
               .user(message)
               .advisors(a -> a.param("conversationId", conversationId))
               .toolCallbacks(allTools)
               .stream()
               .content();
   }
}