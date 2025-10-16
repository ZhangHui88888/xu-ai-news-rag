package com.xu.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.service.QueryService;
import com.xu.news.utils.TestDataBuilder;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QueryController API测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest
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
        queryRequest = TestDataBuilder.createQueryRequest();
        
        queryResponse = new QueryResponse();
        queryResponse.setQuery("什么是人工智能？");
        queryResponse.setAnswer("人工智能（AI）是计算机科学的一个分支...");
        queryResponse.setSource("local");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /query/ask - RAG问答成功")
    void testQuery_Success() throws Exception {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.query").value(queryResponse.getQuery()))
                .andExpect(jsonPath("$.data.answer").value(queryResponse.getAnswer()))
                .andExpect(jsonPath("$.data.source").value(queryResponse.getSource()));

        verify(queryService, times(1)).query(any(QueryRequest.class), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /query/ask - 空查询")
    void testQuery_EmptyQuery() throws Exception {
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
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /query/ask - 查询失败")
    void testQuery_Failed() throws Exception {
        // Given
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenThrow(new RuntimeException("查询失败"));

        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("查询失败"));
    }

    @Test
    @DisplayName("POST /query/ask - 未授权访问")
    void testQuery_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /query/ask - 启用联网搜索")
    void testQuery_WithWebSearch() throws Exception {
        // Given
        queryRequest.setEnableWebSearch(true);
        queryResponse.setSource("web");
        
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.source").value("web"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /query/ask - 指定TopK参数")
    void testQuery_WithTopK() throws Exception {
        // Given
        queryRequest.setTopK(10);
        
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(queryService, times(1)).query(any(QueryRequest.class), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("GET /query/history - 查询历史")
    void testGetHistory_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/query/history")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(queryService, times(1)).getHistory(anyLong(), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("DELETE /query/history/{id} - 删除历史记录")
    void testDeleteHistory_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/query/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(queryService, times(1)).deleteHistory(anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /query/ask - 非常长的查询文本")
    void testQuery_VeryLongQuery() throws Exception {
        // Given
        QueryRequest longRequest = new QueryRequest();
        longRequest.setQuery("这是一个非常长的查询文本".repeat(100));
        
        when(queryService.query(any(QueryRequest.class), anyLong()))
                .thenReturn(queryResponse);

        // When & Then
        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longRequest)))
                .andExpect(status().isOk());
    }
}

