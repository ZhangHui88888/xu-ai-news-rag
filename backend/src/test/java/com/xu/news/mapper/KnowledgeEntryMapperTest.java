package com.xu.news.mapper;

import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * KnowledgeEntryMapper 单元测试（使用Mock）
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("知识库Mapper测试")
class KnowledgeEntryMapperTest {

    @Mock
    private KnowledgeEntryMapper knowledgeEntryMapper;

    private KnowledgeEntry testEntry;

    @BeforeEach
    void setUp() {
        testEntry = TestDataBuilder.createKnowledgeEntry();
    }

    @Test
    @DisplayName("插入知识条目")
    void testInsert() {
        // Given
        when(knowledgeEntryMapper.insert(any(KnowledgeEntry.class))).thenReturn(1);

        // When
        int result = knowledgeEntryMapper.insert(testEntry);

        // Then
        assertEquals(1, result);
        verify(knowledgeEntryMapper, times(1)).insert(any(KnowledgeEntry.class));
    }

    @Test
    @DisplayName("根据ID查询")
    void testSelectById() {
        // Given
        when(knowledgeEntryMapper.selectById(1L)).thenReturn(testEntry);

        // When
        KnowledgeEntry result = knowledgeEntryMapper.selectById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testEntry.getTitle(), result.getTitle());
        verify(knowledgeEntryMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("更新知识条目")
    void testUpdateById() {
        // Given
        when(knowledgeEntryMapper.updateById(any(KnowledgeEntry.class))).thenReturn(1);

        // When
        int result = knowledgeEntryMapper.updateById(testEntry);

        // Then
        assertEquals(1, result);
        verify(knowledgeEntryMapper, times(1)).updateById(any(KnowledgeEntry.class));
    }

    @Test
    @DisplayName("删除知识条目")
    void testDeleteById() {
        // Given
        when(knowledgeEntryMapper.deleteById(1L)).thenReturn(1);

        // When
        int result = knowledgeEntryMapper.deleteById(1L);

        // Then
        assertEquals(1, result);
        verify(knowledgeEntryMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("根据向量ID查询")
    void testFindByVectorIds() {
        // Given
        List<Long> vectorIds = Arrays.asList(1L, 2L);
        List<KnowledgeEntry> entries = Arrays.asList(testEntry);
        when(knowledgeEntryMapper.findByVectorIds(vectorIds)).thenReturn(entries);

        // When
        List<KnowledgeEntry> result = knowledgeEntryMapper.findByVectorIds(vectorIds);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(knowledgeEntryMapper, times(1)).findByVectorIds(vectorIds);
    }

    @Test
    @DisplayName("增加浏览次数")
    void testIncrementViewCount() {
        // Given
        when(knowledgeEntryMapper.incrementViewCount(1L)).thenReturn(1);

        // When
        int result = knowledgeEntryMapper.incrementViewCount(1L);

        // Then
        assertEquals(1, result);
        verify(knowledgeEntryMapper, times(1)).incrementViewCount(1L);
    }
}

