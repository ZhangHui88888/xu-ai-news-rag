package com.xu.news.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 端到端工作流集成测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("端到端工作流测试")
class EndToEndWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String authToken;

    @Test
    @Order(1)
    @DisplayName("完整工作流 - Step 1: 用户注册")
    void step1_UserRegistration() throws Exception {
        // Given
        RegisterRequest registerRequest = TestDataBuilder.createRegisterRequest();

        // When & Then
        MvcResult result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertNotNull(response);
        assertTrue(response.contains("userId"));
    }

    @Test
    @Order(2)
    @DisplayName("完整工作流 - Step 2: 用户登录获取Token")
    void step2_UserLogin() throws Exception {
        // Given
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest();

        // When & Then
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        // 提取token用于后续请求
        authToken = objectMapper.readTree(response)
                .get("data")
                .get("token")
                .asText();
        
        assertNotNull(authToken);
        assertFalse(authToken.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("完整工作流 - Step 3: 查询知识库列表")
    void step3_ListKnowledge() throws Exception {
        // When & Then
        mockMvc.perform(get("/knowledge/list")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("完整工作流 - Step 4: RAG智能问答")
    void step4_RAGQuery() throws Exception {
        // Given
        QueryRequest queryRequest = TestDataBuilder.createQueryRequest();

        // When & Then
        mockMvc.perform(post("/query/ask")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").exists());
    }

    @Test
    @Order(5)
    @DisplayName("完整工作流 - Step 5: 查询历史记录")
    void step5_QueryHistory() throws Exception {
        // When & Then
        mockMvc.perform(get("/query/history")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    @DisplayName("完整工作流 - Step 6: 健康检查")
    void step6_HealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("XU-News-AI-RAG is running!"));
    }

    @Test
    @DisplayName("异常流程 - 无效Token访问")
    void testInvalidTokenAccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/knowledge/list")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("异常流程 - 未授权访问")
    void testUnauthorizedAccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/knowledge/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("异常流程 - 重复注册相同用户名")
    void testDuplicateRegistration() throws Exception {
        // Given
        RegisterRequest registerRequest = TestDataBuilder.createRegisterRequest();

        // 第一次注册
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 第二次注册（重复）
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }
}

