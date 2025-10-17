package com.xu.news.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.service.KnowledgeEntryService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * KnowledgeController 单元测试（使用Mock）
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("知识库控制器测试")
class KnowledgeControllerTest {

    @Mock
    private KnowledgeEntryService knowledgeEntryService;

    @InjectMocks
    private KnowledgeController knowledgeController;

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
    @DisplayName("分页查询知识库列表")
    void testPage() {
        // Given
        when(knowledgeEntryService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        var result = knowledgeEntryService.page(new Page<>(1, 10), new QueryWrapper<>());

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(knowledgeEntryService, times(1)).page(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID获取知识条目")
    void testGetById() {
        // Given
        when(knowledgeEntryService.getById(1L)).thenReturn(testEntry);

        // When
        KnowledgeEntry result = knowledgeEntryService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testEntry.getTitle(), result.getTitle());
        verify(knowledgeEntryService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("删除知识条目")
    void testDelete() {
        // Given
        when(knowledgeEntryService.deleteWithVector(1L)).thenReturn(true);

        // When
        boolean result = knowledgeEntryService.deleteWithVector(1L);

        // Then
        assertTrue(result);
        verify(knowledgeEntryService, times(1)).deleteWithVector(1L);
    }
}

