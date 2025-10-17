package com.xu.news.integration;

import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.service.KnowledgeEntryService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 知识库管理集成测试（使用Mock）
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("知识库管理集成测试")
class KnowledgeManagementIntegrationTest {

    @Mock
    private KnowledgeEntryService knowledgeEntryService;

    private KnowledgeEntry testEntry;

    @BeforeEach
    void setUp() {
        testEntry = TestDataBuilder.createKnowledgeEntry();
    }

    @Test
    @DisplayName("完整CRUD流程")
    void testCompleteCRUD() throws IOException {
        // Create
        when(knowledgeEntryService.createWithVector(any(KnowledgeEntry.class)))
                .thenReturn(testEntry);
        KnowledgeEntry created = knowledgeEntryService.createWithVector(testEntry);
        assertNotNull(created);

        // Read
        when(knowledgeEntryService.getById(1L)).thenReturn(testEntry);
        KnowledgeEntry found = knowledgeEntryService.getById(1L);
        assertNotNull(found);

        // Update
        when(knowledgeEntryService.updateById(any(KnowledgeEntry.class))).thenReturn(true);
        boolean updated = knowledgeEntryService.updateById(testEntry);
        assertTrue(updated);

        // Delete
        when(knowledgeEntryService.deleteWithVector(1L)).thenReturn(true);
        boolean deleted = knowledgeEntryService.deleteWithVector(1L);
        assertTrue(deleted);

        verify(knowledgeEntryService, times(1)).createWithVector(any(KnowledgeEntry.class));
        verify(knowledgeEntryService, times(1)).getById(1L);
        verify(knowledgeEntryService, times(1)).updateById(any(KnowledgeEntry.class));
        verify(knowledgeEntryService, times(1)).deleteWithVector(1L);
    }

    @Test
    @DisplayName("创建失败 - 向量生成异常")
    void testCreate_VectorError() throws IOException {
        // Given
        when(knowledgeEntryService.createWithVector(any(KnowledgeEntry.class)))
                .thenThrow(new IOException("向量生成失败"));

        // When & Then
        assertThrows(IOException.class, () -> {
            knowledgeEntryService.createWithVector(testEntry);
        });
    }
}

