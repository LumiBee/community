package com.lumibee.hive;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 演示 Spring Boot DevTools 类加载器问题
 */
@SpringBootTest
public class ClassLoaderTest {

    @Test
    public void demonstrateClassLoaderIssue() {
        // 模拟从缓存获取的对象（RestartClassLoader 加载）
        List<Object> cachedObjects = new ArrayList<>();
        cachedObjects.add(new MockArticleDetailsDTO(1L));
        cachedObjects.add(new MockArticleDetailsDTO(2L));
        
        System.out.println("=== 传统 for 循环（可以工作）===");
        List<Long> userIds1 = new ArrayList<>();
        for (Object obj : cachedObjects) {
            if (obj instanceof MockArticleDetailsDTO) {
                MockArticleDetailsDTO article = (MockArticleDetailsDTO) obj;
                userIds1.add(article.getUserId());
            }
        }
        System.out.println("用户ID列表: " + userIds1);
        
        System.out.println("\n=== Stream API（会出问题）===");
        try {
            // 这里会抛出 ClassCastException
            List<Long> userIds2 = cachedObjects.stream()
                    .map(obj -> ((MockArticleDetailsDTO) obj).getUserId())
                    .collect(Collectors.toList());
            System.out.println("用户ID列表: " + userIds2);
        } catch (ClassCastException e) {
            System.out.println("ClassCastException: " + e.getMessage());
        }
    }
    
    // 模拟 ArticleDetailsDTO
    static class MockArticleDetailsDTO {
        private Long userId;
        
        public MockArticleDetailsDTO(Long userId) {
            this.userId = userId;
        }
        
        public Long getUserId() {
            return userId;
        }
    }
}
