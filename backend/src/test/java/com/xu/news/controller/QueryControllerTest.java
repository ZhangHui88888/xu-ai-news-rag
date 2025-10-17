package com.xu.news.controller;

import com.xu.news.base.BaseControllerTest;
import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.service.QueryService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QueryController API测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("智能问答控制器API测试")
class QueryControllerTest extends BaseControllerTest {

    @MockBean
    private QueryService queryService;

    private QueryRequest queryRequest;
    private QueryResponse queryResponse;

    @BeforeEach
    void setUp() {
        queryRequest = TestDataBuilder.createQueryRequest();
        
        queryResponse = new QueryResponse();
        queryResponse.setAnswer("人工智能是计算机科学的一个分支...");
        queryResponse.setRetrievedEntries(new ArrayList<>());
        queryResponse.setResponseTimeMs(150L);
    }

    @Test
    @DisplayName("POST /query/ask - 智能问答成功")
    void testAsk_Success() throws Exception {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then
        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").value(queryResponse.getAnswer()))
                .andExpect(jsonPath("$.data.responseTimeMs").exists());

        verify(queryService, times(1)).query(any(QueryRequest.class), anyLong());
    }

    @Test
    @DisplayName("POST /query/ask - 查询失败")
    void testAsk_Failed() throws Exception {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenThrow(new IOException("向量服务不可用"));

        // When & Then
        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());

        verify(queryService, times(1)).query(any(QueryRequest.class), anyLong());
    }

    @Test
    @DisplayName("POST /query/search - 语义搜索成功")
    void testSearch_Success() throws Exception {
        // Given
        queryResponse.setAnswer(null);  // 搜索不返回答案
        when(queryService.semanticSearch(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then
        mockMvc.perform(post("/query/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.retrievedEntries").isArray());

        verify(queryService, times(1)).semanticSearch(any(QueryRequest.class), anyLong());
    }

    @Test
    @DisplayName("POST /query/search - 搜索失败")
    void testSearch_Failed() throws Exception {
        // Given
        when(queryService.semanticSearch(any(QueryRequest.class), anyLong()))
                .thenThrow(new IOException("向量生成失败"));

        // When & Then
        mockMvc.perform(post("/query/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /query/ask - 参数校验失败")
    void testAsk_ValidationFailed() throws Exception {
        // Given
        queryRequest.setQuery("");  // 空查询

        // When & Then
        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());

        verify(queryService, never()).query(any(QueryRequest.class), anyLong());
    }

    @Test
    @DisplayName("POST /query/ask - 未认证用户")
    void testAsk_UnauthorizedUser() throws Exception {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then - 即使没有token，也应该允许访问（使用默认userId）
        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

