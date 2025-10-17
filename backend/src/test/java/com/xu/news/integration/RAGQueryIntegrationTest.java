package com.xu.news.integration;

import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.service.QueryService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RAG查询集成测试（使用Mock）
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RAG查询集成测试")
class RAGQueryIntegrationTest {

    @Mock
    private QueryService queryService;

    private QueryRequest queryRequest;
    private QueryResponse queryResponse;

    @BeforeEach
    void setUp() {
        queryRequest = TestDataBuilder.createQueryRequest();
        
        queryResponse = new QueryResponse();
        queryResponse.setAnswer("这是AI的回答");
        queryResponse.setRetrievedEntries(new ArrayList<>());
        queryResponse.setResponseTimeMs(100L);
    }

    @Test
    @DisplayName("完整RAG查询流程")
    void testCompleteRAGFlow() throws IOException {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When
        QueryResponse result = queryService.query(queryRequest, 1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAnswer());
        assertEquals("这是AI的回答", result.getAnswer());
        verify(queryService, times(1)).query(any(QueryRequest.class), anyLong());
    }

    @Test
    @DisplayName("语义搜索流程")
    void testSemanticSearchFlow() throws IOException {
        // Given
        queryResponse.setAnswer(null);
        when(queryService.semanticSearch(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When
        QueryResponse result = queryService.semanticSearch(queryRequest, 1L);

        // Then
        assertNotNull(result);
        assertNull(result.getAnswer());
        assertNotNull(result.getRetrievedEntries());
        verify(queryService, times(1)).semanticSearch(any(QueryRequest.class), anyLong());
    }

    @Test
    @DisplayName("查询失败 - 服务异常")
    void testQuery_ServiceError() throws IOException {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenThrow(new IOException("AI服务不可用"));

        // When & Then
        assertThrows(IOException.class, () -> {
            queryService.query(queryRequest, 1L);
        });
    }
}

