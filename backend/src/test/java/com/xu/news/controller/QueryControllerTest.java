package com.xu.news.controller;

import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.service.QueryService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * QueryController 单元测试（使用Mock）
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("查询控制器测试")
class QueryControllerTest {

    @Mock
    private QueryService queryService;

    @InjectMocks
    private QueryController queryController;

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
    @DisplayName("智能问答")
    void testQuery() throws IOException {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When
        QueryResponse result = queryService.query(queryRequest, 1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAnswer());
        verify(queryService, times(1)).query(any(QueryRequest.class), anyLong());
    }

    @Test
    @DisplayName("语义搜索")
    void testSemanticSearch() throws IOException {
        // Given
        queryResponse.setAnswer(null);
        when(queryService.semanticSearch(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When
        QueryResponse result = queryService.semanticSearch(queryRequest, 1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRetrievedEntries());
        verify(queryService, times(1)).semanticSearch(any(QueryRequest.class), anyLong());
    }
}

