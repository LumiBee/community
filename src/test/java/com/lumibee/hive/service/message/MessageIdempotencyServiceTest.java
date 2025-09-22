package com.lumibee.hive.service.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 消息幂等性服务测试
 */
@ExtendWith(MockitoExtension.class)
class MessageIdempotencyServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private MessageIdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        // 移除不必要的 stubbing，在需要时单独设置
    }

    @Test
    void testCheckAndSetProcessed_NewMessage_ReturnsTrue() {
        // 准备测试数据
        String businessId = "test_business_id_123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString())).thenReturn(true);

        // 执行测试
        boolean result = idempotencyService.checkAndSetProcessed(businessId);

        // 验证结果
        assertTrue(result);
        verify(valueOperations).setIfAbsent(anyString(), anyString());
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void testCheckAndSetProcessed_DuplicateMessage_ReturnsFalse() {
        // 准备测试数据
        String businessId = "test_business_id_123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString())).thenReturn(false);

        // 执行测试
        boolean result = idempotencyService.checkAndSetProcessed(businessId);

        // 验证结果
        assertFalse(result);
        verify(valueOperations).setIfAbsent(anyString(), anyString());
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void testCheckAndSetProcessed_NullBusinessId_ReturnsTrue() {
        // 执行测试
        boolean result = idempotencyService.checkAndSetProcessed(null);

        // 验证结果
        assertTrue(result);
        verify(valueOperations, never()).setIfAbsent(anyString(), anyString());
    }

    @Test
    void testCheckAndSetProcessed_EmptyBusinessId_ReturnsTrue() {
        // 执行测试
        boolean result = idempotencyService.checkAndSetProcessed("");

        // 验证结果
        assertTrue(result);
        verify(valueOperations, never()).setIfAbsent(anyString(), anyString());
    }

    @Test
    void testIsProcessed_MessageExists_ReturnsTrue() {
        // 准备测试数据
        String businessId = "test_business_id_123";
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // 执行测试
        boolean result = idempotencyService.isProcessed(businessId);

        // 验证结果
        assertTrue(result);
        verify(redisTemplate).hasKey(anyString());
    }

    @Test
    void testIsProcessed_MessageNotExists_ReturnsFalse() {
        // 准备测试数据
        String businessId = "test_business_id_123";
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // 执行测试
        boolean result = idempotencyService.isProcessed(businessId);

        // 验证结果
        assertFalse(result);
        verify(redisTemplate).hasKey(anyString());
    }

    @Test
    void testClearProcessed_Success() {
        // 准备测试数据
        String businessId = "test_business_id_123";
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // 执行测试
        idempotencyService.clearProcessed(businessId);

        // 验证结果
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testClearProcessedBatch_Success() {
        // 准备测试数据
        java.util.List<String> businessIds = java.util.List.of("id1", "id2", "id3");
        when(redisTemplate.delete(any(java.util.Collection.class))).thenReturn(3L);

        // 执行测试
        idempotencyService.clearProcessedBatch(businessIds);

        // 验证结果
        verify(redisTemplate).delete(any(java.util.Collection.class));
    }

    @Test
    void testGetProcessStatus_MessageExists_ReturnsProcessedStatus() {
        // 准备测试数据
        String businessId = "test_business_id_123";
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        when(redisTemplate.getExpire(anyString(), any())).thenReturn(3600L);

        // 执行测试
        MessageIdempotencyService.MessageProcessStatus status = idempotencyService.getProcessStatus(businessId);

        // 验证结果
        assertTrue(status.isProcessed());
        assertEquals(3600L, status.getTtlSeconds());
        assertNull(status.getError());
    }

    @Test
    void testGetProcessStatus_MessageNotExists_ReturnsNotProcessedStatus() {
        // 准备测试数据
        String businessId = "test_business_id_123";
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // 执行测试
        MessageIdempotencyService.MessageProcessStatus status = idempotencyService.getProcessStatus(businessId);

        // 验证结果
        assertFalse(status.isProcessed());
        assertNull(status.getTtlSeconds());
        assertNull(status.getError());
    }

    @Test
    void testCheckAndSetProcessed_Exception_ReturnsTrue() {
        // 准备测试数据
        String businessId = "test_business_id_123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString())).thenThrow(new RuntimeException("Redis error"));

        // 执行测试
        boolean result = idempotencyService.checkAndSetProcessed(businessId);

        // 验证结果
        assertTrue(result); // 异常情况下应该返回 true 以允许处理消息
    }
}
