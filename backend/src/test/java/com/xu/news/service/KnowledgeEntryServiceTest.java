package com.xu.news.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xu.news.dto.SearchRequest;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.mapper.KnowledgeEntryMapper;
import com.xu.news.service.impl.KnowledgeEntryServiceImpl;
import com.xu.news.util.OllamaClient;
import com.xu.news.util.VectorStore;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * KnowledgeEntryService 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("知识条目服务测试")
class KnowledgeEntryServiceTest {

    @Mock
    private KnowledgeEntryMapper knowledgeEntryMapper;

    @Mock
    private OllamaClient ollamaClient;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private KnowledgeEntryServiceImpl knowledgeEntryService;

    private KnowledgeEntry testEntry;
    private SearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        testEntry = TestDataBuilder.createKnowledgeEntry();
        
        searchRequest = new SearchRequest();
        searchRequest.setKeyword("人工智能");
        searchRequest.setCurrent(1L);
        searchRequest.setSize(10L);
    }

    @Test
    @DisplayName("创建知识条目并生成向量 - 成功")
    void testCreateWithVector_Success() throws IOException {
        // Given
        float[] mockVector = new float[768];
        Arrays.fill(mockVector, 0.1f);
        
        when(ollamaClient.generateEmbedding(anyString())).thenReturn(mockVector);
        when(vectorStore.addVector(anyLong(), any(float[].class))).thenReturn(true);
        when(knowledgeEntryMapper.insert(any(KnowledgeEntry.class))).thenReturn(1);

        // When
        KnowledgeEntry result = knowledgeEntryService.createWithVector(testEntry);

        // Then
        assertNotNull(result);
        verify(ollamaClient, times(1)).generateEmbedding(anyString());
        verify(vectorStore, times(1)).addVector(anyLong(), any(float[].class));
        verify(knowledgeEntryMapper, times(1)).insert(any(KnowledgeEntry.class));
    }

    @Test
    @DisplayName("创建知识条目并生成向量 - 向量生成失败")
    void testCreateWithVector_EmbeddingFailed() throws IOException {
        // Given
        when(ollamaClient.generateEmbedding(anyString()))
                .thenThrow(new IOException("向量生成失败"));

        // When & Then
        assertThrows(IOException.class, () -> {
            knowledgeEntryService.createWithVector(testEntry);
        });
        verify(vectorStore, never()).addVector(anyLong(), any(float[].class));
        verify(knowledgeEntryMapper, never()).insert(any(KnowledgeEntry.class));
    }

    @Test
    @DisplayName("搜索知识条目 - 成功")
    void testSearch_Success() {
        // Given
        Page<KnowledgeEntry> mockPage = new Page<>(1, 10);
        List<KnowledgeEntry> mockList = new ArrayList<>();
        mockList.add(testEntry);
        mockPage.setRecords(mockList);
        mockPage.setTotal(1);
        
        when(knowledgeEntryMapper.searchByKeyword(any(Page.class), anyString(), anyString(), 
                anyLong(), anyLong(), any(), any()))
                .thenReturn(mockPage);

        // When
        Page<KnowledgeEntry> result = knowledgeEntryService.search(searchRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(knowledgeEntryMapper, times(1)).searchByKeyword(any(), anyString(), anyString(), 
                anyLong(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("根据向量ID列表查询知识条目 - 成功")
    void testFindByVectorIds_Success() {
        // Given
        List<Long> vectorIds = Arrays.asList(1L, 2L, 3L);
        List<KnowledgeEntry> mockEntries = Arrays.asList(
                testEntry,
                TestDataBuilder.createKnowledgeEntry(),
                TestDataBuilder.createKnowledgeEntry()
        );
        
        when(knowledgeEntryMapper.selectBatchIds(vectorIds)).thenReturn(mockEntries);

        // When
        List<KnowledgeEntry> result = knowledgeEntryService.findByVectorIds(vectorIds);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(knowledgeEntryMapper, times(1)).selectBatchIds(vectorIds);
    }

    @Test
    @DisplayName("根据向量ID列表查询知识条目 - 空列表")
    void testFindByVectorIds_EmptyList() {
        // Given
        List<Long> vectorIds = new ArrayList<>();

        // When
        List<KnowledgeEntry> result = knowledgeEntryService.findByVectorIds(vectorIds);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(knowledgeEntryMapper, never()).selectBatchIds(any());
    }

    @Test
    @DisplayName("删除知识条目并删除向量 - 成功")
    void testDeleteWithVector_Success() {
        // Given
        Long entryId = 1L;
        when(knowledgeEntryMapper.selectById(entryId)).thenReturn(testEntry);
        when(vectorStore.deleteVector(entryId)).thenReturn(true);
        when(knowledgeEntryMapper.deleteById(entryId)).thenReturn(1);

        // When
        boolean result = knowledgeEntryService.deleteWithVector(entryId);

        // Then
        assertTrue(result);
        verify(vectorStore, times(1)).deleteVector(entryId);
        verify(knowledgeEntryMapper, times(1)).deleteById(entryId);
    }

    @Test
    @DisplayName("删除知识条目 - 条目不存在")
    void testDeleteWithVector_EntryNotFound() {
        // Given
        Long entryId = 999L;
        when(knowledgeEntryMapper.selectById(entryId)).thenReturn(null);

        // When
        boolean result = knowledgeEntryService.deleteWithVector(entryId);

        // Then
        assertFalse(result);
        verify(vectorStore, never()).deleteVector(anyLong());
        verify(knowledgeEntryMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("增加浏览次数 - 成功")
    void testIncrementViewCount_Success() {
        // Given
        Long entryId = 1L;
        testEntry.setViewCount(10);
        
        when(knowledgeEntryMapper.selectById(entryId)).thenReturn(testEntry);
        when(knowledgeEntryMapper.updateById(any(KnowledgeEntry.class))).thenReturn(1);

        // When
        knowledgeEntryService.incrementViewCount(entryId);

        // Then
        verify(knowledgeEntryMapper, times(1)).selectById(entryId);
        verify(knowledgeEntryMapper, times(1)).updateById(argThat(entry -> 
                entry.getViewCount() == 11
        ));
    }

    @Test
    @DisplayName("生成摘要 - 成功")
    void testGenerateSummary_Success() throws IOException {
        // Given
        String content = "这是一篇很长的文章内容，需要生成摘要。人工智能技术在各个领域都有广泛应用...";
        String mockSummary = "AI技术广泛应用于各个领域";
        
        when(ollamaClient.generateText(anyString())).thenReturn(mockSummary);

        // When
        String result = knowledgeEntryService.generateSummary(content);

        // Then
        assertNotNull(result);
        assertEquals(mockSummary, result);
        verify(ollamaClient, times(1)).generateText(anyString());
    }

    @Test
    @DisplayName("生成摘要 - AI服务异常")
    void testGenerateSummary_AIServiceError() throws IOException {
        // Given
        String content = "测试内容";
        when(ollamaClient.generateText(anyString()))
                .thenThrow(new IOException("AI服务不可用"));

        // When & Then
        assertThrows(IOException.class, () -> {
            knowledgeEntryService.generateSummary(content);
        });
    }

    @Test
    @DisplayName("生成摘要 - 内容为空")
    void testGenerateSummary_EmptyContent() throws IOException {
        // Given
        String content = "";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            knowledgeEntryService.generateSummary(content);
        });
        verify(ollamaClient, never()).generateText(anyString());
    }
}

