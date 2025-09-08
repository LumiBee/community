package com.lumibee.hive.agent;

import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.lumibee.hive.advisors.MyLoggerAdvisor;
import com.lumibee.hive.rag.QueryRewriter;
import com.lumibee.hive.utils.TransApi;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(name = "baidu.translate.app-id")
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
               .maxMessages(100)
               .build();

       this.chatClient = builder
               .defaultAdvisors(new MyLoggerAdvisor())
               .build();
   }

   public String doChatWithRag(String message, String conversationId) {
       log.info("JavaApp.doChatWithRag: message={}, conversationId={}", message, conversationId);
       try {
           // 暂时禁用QueryRewriter，直接使用原始消息避免字体错误
           String translatedMessage = message;
           log.info("Using original message: {}", translatedMessage);

           // 使用流式处理，收集所有结果
           StringBuilder result = new StringBuilder();
           
           try {
               chatClient.prompt(javaPromptTemplate.create())
                       .user(translatedMessage)
                       .advisors(a -> a.param("conversationId", conversationId))
                       .stream()
                       .content()
                       .doOnNext(chunk -> {
                           result.append(chunk);
                           log.debug("收到AI响应片段: {}", chunk);
                       })
                       .doOnComplete(() -> log.info("AI响应完成"))
                       .doOnError(error -> log.error("AI响应错误: {}", error.getMessage(), error))
                       .blockLast(Duration.ofMinutes(5)); // 5分钟超时，给足够时间生成完整内容
               
               String finalResult = result.toString();
               if (finalResult.isEmpty()) {
                   log.warn("AI响应为空");
                   return "抱歉，AI助手暂时无法响应，请稍后再试。";
               }
               
               log.info("AI响应成功，长度: {}", finalResult.length());
               return finalResult;
               
           } catch (Exception aiError) {
               log.error("AI调用失败: {}", aiError.getMessage(), aiError);
               return "抱歉，AI助手暂时无法响应，请稍后再试。";
           }
       } catch (Exception e) {
           log.error("Java指导对话失败: {}", e.getMessage(), e);
           return "抱歉，AI助手暂时无法响应，请稍后再试。";
       }
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