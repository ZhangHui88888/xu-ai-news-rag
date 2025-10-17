package com.xu.news.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xu.news.base.BaseControllerTest;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.service.KnowledgeEntryService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KnowledgeController API测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("知识库控制器API测试")
class KnowledgeControllerTest extends BaseControllerTest {

    @MockBean
    private KnowledgeEntryService knowledgeEntryService;

    private KnowledgeEntry testEntry;
    private Page<KnowledgeEntry> mockPage;

    @BeforeEach
    void setUp() {
        testEntry = TestDataBuilder.createKnowledgeEntry();
        
        mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testEntry));
        mockPage.setTotal(1);
    }

    @Test
    @DisplayName("GET /knowledge/list - 获取列表成功")
    void testList_Success() throws Exception {
        // Given
        when(knowledgeEntryService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(1));

        verify(knowledgeEntryService, times(1)).page(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("GET /knowledge/list - 带关键词搜索")
    void testList_WithKeyword() throws Exception {
        // Given
        when(knowledgeEntryService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "人工智能")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        verify(knowledgeEntryService, times(1)).page(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("GET /knowledge/list - 带类型过滤")
    void testList_WithContentType() throws Exception {
        // Given
        when(knowledgeEntryService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("contentType", "news")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /knowledge - 创建知识条目成功")
    void testCreate_Success() throws Exception {
        // Given
        when(knowledgeEntryService.createWithVector(any(KnowledgeEntry.class)))
                .thenReturn(testEntry);

        // When & Then
        mockMvc.perform(post("/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(testEntry))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value(testEntry.getTitle()));

        verify(knowledgeEntryService, times(1)).createWithVector(any(KnowledgeEntry.class));
    }

    @Test
    @DisplayName("POST /knowledge - 创建失败")
    void testCreate_Failed() throws Exception {
        // Given
        when(knowledgeEntryService.createWithVector(any(KnowledgeEntry.class)))
                .thenThrow(new RuntimeException("向量生成失败"));

        // When & Then
        mockMvc.perform(post("/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(testEntry))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("GET /knowledge/{id} - 获取详情成功")
    void testGetById_Success() throws Exception {
        // Given
        Long entryId = 1L;
        when(knowledgeEntryService.getById(entryId)).thenReturn(testEntry);

        // When & Then
        mockMvc.perform(get("/knowledge/" + entryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(entryId))
                .andExpect(jsonPath("$.data.title").value(testEntry.getTitle()));

        verify(knowledgeEntryService, times(1)).getById(entryId);
        verify(knowledgeEntryService, times(1)).incrementViewCount(entryId);
    }

    @Test
    @DisplayName("GET /knowledge/{id} - 条目不存在")
    void testGetById_NotFound() throws Exception {
        // Given
        Long entryId = 999L;
        when(knowledgeEntryService.getById(entryId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/knowledge/" + entryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        verify(knowledgeEntryService, never()).incrementViewCount(anyLong());
    }

    @Test
    @DisplayName("PUT /knowledge/{id} - 更新成功")
    void testUpdate_Success() throws Exception {
        // Given
        Long entryId = 1L;
        when(knowledgeEntryService.updateById(any(KnowledgeEntry.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/knowledge/" + entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(testEntry))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(knowledgeEntryService, times(1)).updateById(any(KnowledgeEntry.class));
    }

    @Test
    @DisplayName("DELETE /knowledge/{id} - 删除成功")
    void testDelete_Success() throws Exception {
        // Given
        Long entryId = 1L;
        when(knowledgeEntryService.deleteWithVector(entryId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/knowledge/" + entryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(knowledgeEntryService, times(1)).deleteWithVector(entryId);
    }

    @Test
    @DisplayName("DELETE /knowledge/{id} - 删除失败")
    void testDelete_Failed() throws Exception {
        // Given
        Long entryId = 999L;
        when(knowledgeEntryService.deleteWithVector(entryId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/knowledge/" + entryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("GET /knowledge/list - 带时间范围过滤")
    void testList_WithDateRange() throws Exception {
        // Given
        when(knowledgeEntryService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /knowledge/list - 空列表")
    void testList_Empty() throws Exception {
        // Given
        Page<KnowledgeEntry> emptyPage = new Page<>(1, 10);
        emptyPage.setRecords(new ArrayList<>());
        emptyPage.setTotal(0);
        
        when(knowledgeEntryService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isEmpty());
    }
}

