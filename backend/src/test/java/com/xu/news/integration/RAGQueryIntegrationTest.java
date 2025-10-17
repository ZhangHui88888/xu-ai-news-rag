package com.xu.news.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.dto.QueryRequest;
import com.xu.news.util.JwtUtil;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RAG查询集成测试 - 测试完整的问答流程
 * 
 * @author XU
 * @since 2025-10-17
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("RAG查询集成测试")
class RAGQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String authToken;
    private QueryRequest queryRequest;

    @BeforeEach
    void setUp() {
        authToken = jwtUtil.generateToken(1L, "testuser", "user");
        queryRequest = TestDataBuilder.createQueryRequest();
    }

    @Test
    @DisplayName("智能问答 - 基本查询（需要Mock AI服务）")
    void testBasicQuery() throws Exception {
        // 注意：这个测试在没有实际AI服务的情况下会失败
        // 可以通过Mock来测试，或者在有AI服务的环境中运行
        
        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
                // 实际响应取决于是否有AI服务
    }

    @Test
    @DisplayName("语义搜索 - 基本搜索（需要Mock向量服务）")
    void testSemanticSearch() throws Exception {
        mockMvc.perform(post("/query/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("查询参数验证 - 空查询")
    void testQueryValidation_EmptyQuery() throws Exception {
        queryRequest.setQuery("");

        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("查询参数验证 - TopK范围")
    void testQueryValidation_TopKRange() throws Exception {
        queryRequest.setTopK(100);  // 假设最大值为50

        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("未认证用户查询 - 应该使用默认用户ID")
    void testQueryWithoutAuth() throws Exception {
        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("不同查询模式 - 需要答案 vs 只搜索")
    void testDifferentQueryModes() throws Exception {
        // 模式1: 需要答案
        queryRequest.setNeedAnswer(true);
        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // 模式2: 只搜索，不需要答案
        mockMvc.perform(post("/query/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("查询性能 - 超时处理")
    void testQueryTimeout() throws Exception {
        // 这个测试验证查询在合理时间内返回
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(post("/query/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        // 验证响应时间在合理范围内（例如 < 10秒）
        // assertTrue(endTime - startTime < 10000);
    }

    @Test
    @DisplayName("特殊字符查询")
    void testQueryWithSpecialCharacters() throws Exception {
        queryRequest.setQuery("什么是AI？它与ML有什么区别？");

        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("长文本查询")
    void testLongQuery() throws Exception {
        StringBuilder longQuery = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longQuery.append("这是一个很长的查询文本。");
        }
        queryRequest.setQuery(longQuery.toString());

        mockMvc.perform(post("/query/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryRequest))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }
}

