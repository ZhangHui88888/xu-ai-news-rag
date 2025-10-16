package com.xu.news.util;

import com.xu.news.utils.MockDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OllamaClient 单元测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Ollama客户端测试")
class OllamaClientTest {

    @Mock
    private RestTemplate restTemplate;

    private OllamaClient ollamaClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ollamaClient = new OllamaClient("http://localhost:11434", restTemplate);
    }

    @Test
    @DisplayName("生成文本 - 成功")
    void testGenerate_Success() {
        // Given
        String prompt = "什么是人工智能？";
        String mockResponse = MockDataGenerator.mockOllamaResponse(prompt);
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
            .thenReturn("{\"response\":\"" + mockResponse + "\"}");

        // When
        String response = ollamaClient.generate("qwen3:0.6b", prompt);

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("生成文本 - 空提示")
    void testGenerate_EmptyPrompt() {
        // Given
        String prompt = "";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ollamaClient.generate("qwen3:0.6b", prompt);
        });
    }

    @Test
    @DisplayName("生成文本 - Null提示")
    void testGenerate_NullPrompt() {
        // Given
        String prompt = null;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ollamaClient.generate("qwen3:0.6b", prompt);
        });
    }

    @Test
    @DisplayName("生成Embedding - 成功")
    void testGenerateEmbedding_Success() {
        // Given
        String text = "测试文本";
        float[] mockEmbedding = MockDataGenerator.randomVector(768);
        
        StringBuilder embeddingJson = new StringBuilder("[");
        for (int i = 0; i < mockEmbedding.length; i++) {
            embeddingJson.append(mockEmbedding[i]);
            if (i < mockEmbedding.length - 1) embeddingJson.append(",");
        }
        embeddingJson.append("]");
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
            .thenReturn("{\"embedding\":" + embeddingJson + "}");

        // When
        float[] embedding = ollamaClient.generateEmbedding("nomic-embed-text", text);

        // Then
        assertNotNull(embedding);
        assertEquals(768, embedding.length);
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("生成Embedding - 空文本")
    void testGenerateEmbedding_EmptyText() {
        // Given
        String text = "";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ollamaClient.generateEmbedding("nomic-embed-text", text);
        });
    }

    @Test
    @DisplayName("检查模型是否存在 - 存在")
    void testCheckModel_Exists() {
        // Given
        String modelName = "qwen3:0.6b";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn("{\"models\":[{\"name\":\"qwen3:0.6b\"}]}");

        // When
        boolean exists = ollamaClient.checkModelExists(modelName);

        // Then
        assertTrue(exists);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("检查模型是否存在 - 不存在")
    void testCheckModel_NotExists() {
        // Given
        String modelName = "nonexistent:model";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn("{\"models\":[{\"name\":\"qwen3:0.6b\"}]}");

        // When
        boolean exists = ollamaClient.checkModelExists(modelName);

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("生成文本 - 服务不可用")
    void testGenerate_ServiceUnavailable() {
        // Given
        String prompt = "测试提示";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            ollamaClient.generate("qwen3:0.6b", prompt);
        });
    }

    @Test
    @DisplayName("生成批量Embeddings - 成功")
    void testGenerateBatchEmbeddings_Success() {
        // Given
        String[] texts = {"文本1", "文本2", "文本3"};
        float[] mockEmbedding = MockDataGenerator.randomVector(768);
        
        StringBuilder embeddingJson = new StringBuilder("[");
        for (int i = 0; i < mockEmbedding.length; i++) {
            embeddingJson.append(mockEmbedding[i]);
            if (i < mockEmbedding.length - 1) embeddingJson.append(",");
        }
        embeddingJson.append("]");
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
            .thenReturn("{\"embedding\":" + embeddingJson + "}");

        // When
        float[][] embeddings = ollamaClient.generateBatchEmbeddings("nomic-embed-text", texts);

        // Then
        assertNotNull(embeddings);
        assertEquals(texts.length, embeddings.length);
        for (float[] embedding : embeddings) {
            assertEquals(768, embedding.length);
        }
        verify(restTemplate, times(texts.length)).postForObject(anyString(), any(), eq(String.class));
    }
}

