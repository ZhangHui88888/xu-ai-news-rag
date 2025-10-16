package com.xu.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.service.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QueryController API测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest(classes = com.xu.news.XuNewsApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("查询控制器API测试")
class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QueryService queryService;

    private QueryRequest queryRequest;
    private QueryResponse queryResponse;

    @BeforeEach
    void setUp() {
        queryRequest = new QueryRequest();
        queryRequest.setQuery("什么是人工智能？");
        queryRequest.setTopK(5);
        queryRequest.setNeedAnswer(true);

        queryResponse = new QueryResponse();
        queryResponse.setAnswer("人工智能（AI）是计算机科学的一个分支...");
        queryResponse.setRetrievedEntries(new ArrayList<>());
        queryResponse.setResponseTimeMs(1000L);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /query/ask - RAG问答成功")
    void testAsk_Success() throws Exception {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").value(queryResponse.getAnswer()));

        verify(queryService, times(1)).query(any(QueryRequest.class), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /query/search - 语义搜索")
    void testSearch_Success() throws Exception {
        // Given
        when(queryService.semanticSearch(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then
        mockMvc.perform(post("/query/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(queryService, times(1)).semanticSearch(any(QueryRequest.class), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /query/ask - 空查询")
    void testAsk_EmptyQuery() throws Exception {
        // Given
        QueryRequest emptyRequest = new QueryRequest();
        emptyRequest.setQuery("");

        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /query/ask - 查询失败")
    void testAsk_QueryFailed() throws Exception {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenThrow(new RuntimeException("向量化失败"));

        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("POST /query/ask - 未授权访问")
    void testAsk_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isUnauthorized());
    }
}

