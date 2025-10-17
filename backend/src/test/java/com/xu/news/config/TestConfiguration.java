package com.xu.news.config;

import com.xu.news.util.OllamaClient;
import com.xu.news.util.RerankerClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试环境配置
 * 提供Mock的Bean以避免连接外部服务
 * 
 * @author XU
 * @since 2025-10-17
 */
@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    /**
     * Mock OllamaClient - 避免实际连接Ollama服务
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public OllamaClient mockOllamaClient() {
        try {
            OllamaClient mock = mock(OllamaClient.class);
            
            // Mock generateAnswer
            when(mock.generateAnswer(anyString())).thenReturn("这是Mock的AI回答");
            when(mock.generateAnswer(anyString(), anyList())).thenReturn("这是Mock的AI回答");
            
            // Mock generateEmbedding
            List<Double> mockVector = new ArrayList<>();
            for (int i = 0; i < 768; i++) {
                mockVector.add(Math.random());
            }
            when(mock.generateEmbedding(anyString())).thenReturn(mockVector);
            
            return mock;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock OllamaClient", e);
        }
    }

    /**
     * Mock RerankerClient - 避免实际连接Reranker服务
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RerankerClient mockRerankerClient() {
        try {
            RerankerClient mock = mock(RerankerClient.class);
            
            // Mock isAvailable
            when(mock.isAvailable()).thenReturn(false);
            
            // Mock rerank
            when(mock.rerank(anyString(), anyList(), anyInt())).thenReturn(new ArrayList<>());
            
            return mock;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock RerankerClient", e);
        }
    }
}
