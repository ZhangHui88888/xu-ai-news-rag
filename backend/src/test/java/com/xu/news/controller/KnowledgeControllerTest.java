package com.xu.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.service.KnowledgeEntryService;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KnowledgeController API测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest(classes = com.xu.news.XuNewsApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("知识库控制器API测试")
class KnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KnowledgeEntryService knowledgeEntryService;

    private KnowledgeEntry testEntry;

    @BeforeEach
    void setUp() {
        testEntry = new KnowledgeEntry();
        testEntry.setId(1L);
        testEntry.setTitle("AI技术最新进展");
        testEntry.setContent("人工智能技术在2025年取得了重大突破...");
        testEntry.setSummary("AI在多个领域取得进展");
        testEntry.setContentType("news");
        testEntry.setSourceUrl("https://example.com/ai-news");
        testEntry.setSourceName("TechCrunch");
        testEntry.setAuthor("John Doe");
        testEntry.setPublishedAt(LocalDateTime.now());
        testEntry.setCreatedBy(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /knowledge - 创建知识条目")
    void testCreate_Success() throws Exception {
        // Given
        when(knowledgeEntryService.createWithVector(any(KnowledgeEntry.class)))
                .thenReturn(testEntry);

        // When & Then
        mockMvc.perform(post("/knowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEntry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建成功"));

        verify(knowledgeEntryService, times(1)).createWithVector(any(KnowledgeEntry.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /knowledge/{id} - 查询详情")
    void testGetById_Success() throws Exception {
        // Given
        when(knowledgeEntryService.getById(1L)).thenReturn(testEntry);
        doNothing().when(knowledgeEntryService).incrementViewCount(1L);

        // When & Then
        mockMvc.perform(get("/knowledge/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value(testEntry.getTitle()));

        verify(knowledgeEntryService, times(1)).getById(1L);
        verify(knowledgeEntryService, times(1)).incrementViewCount(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /knowledge/{id} - 知识条目不存在")
    void testGetById_NotFound() throws Exception {
        // Given
        when(knowledgeEntryService.getById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/knowledge/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("知识条目不存在"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /knowledge/{id} - 删除成功")
    void testDelete_Success() throws Exception {
        // Given
        when(knowledgeEntryService.deleteWithVector(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/knowledge/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(knowledgeEntryService, times(1)).deleteWithVector(1L);
    }

    @Test
    @DisplayName("POST /knowledge/import - N8N导入")
    void testImportFromN8N_Success() throws Exception {
        // Given
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("title", "测试标题");
        rawData.put("content", "测试内容");
        rawData.put("summary", "测试摘要");
        rawData.put("sourceUrl", "https://example.com");
        rawData.put("author", "测试作者");
        rawData.put("contentType", "news");

        when(knowledgeEntryService.createWithVector(any(KnowledgeEntry.class)))
                .thenReturn(testEntry);

        // When & Then
        mockMvc.perform(post("/knowledge/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rawData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("导入成功"));

        verify(knowledgeEntryService, times(1)).createWithVector(any(KnowledgeEntry.class));
    }

    @Test
    @DisplayName("POST /knowledge/import - 缺少必填字段")
    void testImportFromN8N_MissingRequiredField() throws Exception {
        // Given - 缺少title
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("content", "测试内容");

        // When & Then
        mockMvc.perform(post("/knowledge/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rawData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("标题不能为空"));
    }
}

