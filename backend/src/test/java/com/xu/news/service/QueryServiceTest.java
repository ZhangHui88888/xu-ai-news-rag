package com.xu.news.service;

import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.entity.UserQueryHistory;
import com.xu.news.mapper.UserQueryHistoryMapper;
import com.xu.news.service.impl.QueryServiceImpl;
import com.xu.news.util.OllamaClient;
import com.xu.news.util.RerankerClient;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * QueryService 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("智能问答服务测试")
class QueryServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private OllamaClient ollamaClient;

    @Mock
    private RerankerClient rerankerClient;

    @Mock
    private KnowledgeEntryService knowledgeEntryService;

    @Mock
    private UserQueryHistoryMapper userQueryHistoryMapper;

    @InjectMocks
    private QueryServiceImpl queryService;

    private QueryRequest queryRequest;
    private KnowledgeEntry testEntry;
    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        queryRequest = TestDataBuilder.createQueryRequest();
        queryRequest.setQuery("什么是人工智能？");
        queryRequest.setTopK(5);
        queryRequest.setNeedAnswer(true);
        
        testEntry = TestDataBuilder.createKnowledgeEntry();
    }

    @Test
    @DisplayName("RAG查询 - 成功")
    void testQuery_Success() throws IOException {
        // Given
        List<Double> mockVector = createTestVector(768);
        
        List<VectorStore.SearchResult> mockSearchResults = new ArrayList<>();
        mockSearchResults.add(new VectorStore.SearchResult(1L, 0.95));
        mockSearchResults.add(new VectorStore.SearchResult(2L, 0.90));
        
        List<KnowledgeEntry> mockEntries = Arrays.asList(testEntry);
        
        String mockAnswer = "人工智能（AI）是计算机科学的一个分支。";
        
        when(ollamaClient.generateEmbedding(anyString())).thenReturn(mockVector);
        when(vectorStore.search(anyList(), anyInt())).thenReturn(mockSearchResults);
        when(knowledgeEntryService.findByVectorIds(anyList())).thenReturn(mockEntries);
        when(ollamaClient.generateAnswer(anyString(), anyList())).thenReturn(mockAnswer);
        when(userQueryHistoryMapper.insert(any(UserQueryHistory.class))).thenReturn(1);

        // When
        QueryResponse response = queryService.query(queryRequest, testUserId);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertEquals(mockAnswer, response.getAnswer());
        assertNotNull(response.getRetrievedEntries());
        
        verify(ollamaClient, times(1)).generateEmbedding(anyString());
        verify(vectorStore, times(1)).search(anyList(), anyInt());
        verify(knowledgeEntryService, times(1)).findByVectorIds(anyList());
        verify(ollamaClient, times(1)).generateAnswer(anyString(), anyList());
    }

    @Test
    @DisplayName("RAG查询 - 不需要生成答案")
    void testQuery_NoAnswerNeeded() throws IOException {
        // Given
        queryRequest.setNeedAnswer(false);
        
        List<Double> mockVector = createTestVector(768);
        
        List<VectorStore.SearchResult> mockSearchResults = new ArrayList<>();
        mockSearchResults.add(new VectorStore.SearchResult(1L, 0.95));
        
        List<KnowledgeEntry> mockEntries = Arrays.asList(testEntry);
        
        when(ollamaClient.generateEmbedding(anyString())).thenReturn(mockVector);
        when(vectorStore.search(anyList(), anyInt())).thenReturn(mockSearchResults);
        when(knowledgeEntryService.findByVectorIds(anyList())).thenReturn(mockEntries);
        when(userQueryHistoryMapper.insert(any(UserQueryHistory.class))).thenReturn(1);

        // When
        QueryResponse response = queryService.query(queryRequest, testUserId);

        // Then
        assertNotNull(response);
        assertNotNull(response.getRetrievedEntries());
        
        verify(ollamaClient, times(1)).generateEmbedding(anyString());
        verify(vectorStore, times(1)).search(anyList(), anyInt());
        verify(ollamaClient, never()).generateAnswer(anyString(), anyList());
    }

    @Test
    @DisplayName("RAG查询 - 未找到相关文档")
    void testQuery_NoDocumentsFound() throws IOException {
        // Given
        List<Double> mockVector = createTestVector(768);
        
        List<VectorStore.SearchResult> mockSearchResults = new ArrayList<>();
        
        when(ollamaClient.generateEmbedding(anyString())).thenReturn(mockVector);
        when(vectorStore.search(anyList(), anyInt())).thenReturn(mockSearchResults);

        // When
        QueryResponse response = queryService.query(queryRequest, testUserId);

        // Then
        assertNotNull(response);
        
        verify(knowledgeEntryService, never()).findByVectorIds(anyList());
        verify(ollamaClient, never()).generateAnswer(anyString(), anyList());
    }

    @Test
    @DisplayName("RAG查询 - 向量生成失败")
    void testQuery_EmbeddingFailed() throws IOException {
        // Given
        when(ollamaClient.generateEmbedding(anyString()))
                .thenThrow(new IOException("向量服务不可用"));

        // When & Then
        assertThrows(IOException.class, () -> {
            queryService.query(queryRequest, testUserId);
        });
        
        verify(vectorStore, never()).search(anyList(), anyInt());
        verify(knowledgeEntryService, never()).findByVectorIds(anyList());
    }

    @Test
    @DisplayName("RAG查询 - LLM生成答案失败")
    void testQuery_LLMFailed() throws IOException {
        // Given
        List<Double> mockVector = createTestVector(768);
        
        List<VectorStore.SearchResult> mockSearchResults = new ArrayList<>();
        mockSearchResults.add(new VectorStore.SearchResult(1L, 0.95));
        
        List<KnowledgeEntry> mockEntries = Arrays.asList(testEntry);
        
        when(ollamaClient.generateEmbedding(anyString())).thenReturn(mockVector);
        when(vectorStore.search(anyList(), anyInt())).thenReturn(mockSearchResults);
        when(knowledgeEntryService.findByVectorIds(anyList())).thenReturn(mockEntries);
        when(ollamaClient.generateAnswer(anyString(), anyList()))
                .thenThrow(new IOException("LLM服务不可用"));

        // When & Then
        assertThrows(IOException.class, () -> {
            queryService.query(queryRequest, testUserId);
        });
    }

    @Test
    @DisplayName("语义搜索 - 成功")
    void testSemanticSearch_Success() throws IOException {
        // Given
        List<Double> mockVector = createTestVector(768);
        
        List<VectorStore.SearchResult> mockSearchResults = new ArrayList<>();
        mockSearchResults.add(new VectorStore.SearchResult(1L, 0.95));
        mockSearchResults.add(new VectorStore.SearchResult(2L, 0.90));
        
        List<KnowledgeEntry> mockEntries = Arrays.asList(testEntry);
        
        when(ollamaClient.generateEmbedding(anyString())).thenReturn(mockVector);
        when(vectorStore.search(anyList(), anyInt())).thenReturn(mockSearchResults);
        when(knowledgeEntryService.findByVectorIds(anyList())).thenReturn(mockEntries);

        // When
        QueryResponse response = queryService.semanticSearch(queryRequest, testUserId);

        // Then
        assertNotNull(response);
        assertNotNull(response.getRetrievedEntries());
        
        verify(ollamaClient, times(1)).generateEmbedding(anyString());
        verify(vectorStore, times(1)).search(anyList(), anyInt());
        verify(knowledgeEntryService, times(1)).findByVectorIds(anyList());
        verify(ollamaClient, never()).generateAnswer(anyString(), anyList());
    }

    /**
     * 创建测试用的向量
     */
    private List<Double> createTestVector(int dimension) {
        return IntStream.range(0, dimension)
                .mapToDouble(i -> Math.random())
                .boxed()
                .collect(Collectors.toList());
    }
}

