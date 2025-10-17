package com.xu.news.config;

import com.xu.news.util.OllamaClient;
import com.xu.news.util.RerankerClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试配置 - 提供Mock的Bean
 * 
 * @author XU
 * @since 2025-10-17
 */
@TestConfiguration
public class TestConfig {

    /**
     * Mock OllamaClient - 避免实际连接Ollama服务
     */
    @Bean
    @Primary
    public OllamaClient mockOllamaClient() throws IOException {
        OllamaClient mock = mock(OllamaClient.class);
        
        // Mock generateAnswer
        when(mock.generateAnswer(anyString())).thenReturn("这是Mock的AI回答");
        when(mock.generateAnswer(anyString(), anyList())).thenReturn("这是Mock的AI回答");
        
        // Mock generateEmbedding
        List<Double> mockVector = IntStream.range(0, 768)
                .mapToDouble(i -> Math.random())
                .boxed()
                .collect(Collectors.toList());
        when(mock.generateEmbedding(anyString())).thenReturn(mockVector);
        
        return mock;
    }

    /**
     * Mock RerankerClient - 避免实际连接Reranker服务
     */
    @Bean
    @Primary
    public RerankerClient mockRerankerClient() throws IOException {
        RerankerClient mock = mock(RerankerClient.class);
        
        // Mock isAvailable
        when(mock.isAvailable()).thenReturn(false);
        
        // Mock rerank
        when(mock.rerank(anyString(), anyList(), anyInt())).thenReturn(new ArrayList<>());
        
        return mock;
    }
}

