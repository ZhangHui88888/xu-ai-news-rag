package com.xu.news.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.QueryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 安全性集成测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest(classes = com.xu.news.XuNewsApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("安全性测试")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("公开端点 - 健康检查无需认证")
    void testPublicEndpoint_Health() throws Exception {
        mockMvc.perform(get("/auth/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("公开端点 - 登录无需认证")
    void testPublicEndpoint_Login() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Test123456");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("保护端点 - 未授权访问返回401")
    void testProtectedEndpoint_Unauthorized() throws Exception {
        QueryRequest request = new QueryRequest();
        request.setQuery("测试查询");

        mockMvc.perform(post("/query/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("保护端点 - 无效Token返回401")
    void testProtectedEndpoint_InvalidToken() throws Exception {
        QueryRequest request = new QueryRequest();
        request.setQuery("测试查询");

        mockMvc.perform(post("/query/ask")
                .header("Authorization", "Bearer invalid-token-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("N8N导入端点 - 无需认证")
    void testN8NImport_NoAuth() throws Exception {
        Map<String, Object> importData = new HashMap<>();
        importData.put("title", "测试标题");
        importData.put("content", "测试内容");

        // N8N导入端点应该允许无认证访问
        mockMvc.perform(post("/knowledge/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(importData)))
                .andExpect(status().isOk());
    }
}

