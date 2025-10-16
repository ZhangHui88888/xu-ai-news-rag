package com.xu.news.util;

import com.xu.news.utils.MockDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VectorStore 单元测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("向量存储测试")
class VectorStoreTest {

    @TempDir
    Path tempDir;

    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        // 使用临时目录初始化向量存储
        String indexPath = tempDir.resolve("test_faiss_index").toString();
        vectorStore = new VectorStore(indexPath, 768);
    }

    @Test
    @DisplayName("初始化向量存储 - 成功")
    void testInitialize_Success() {
        // Then
        assertNotNull(vectorStore);
    }

    @Test
    @DisplayName("添加向量 - 单个向量")
    void testAddVector_SingleVector() {
        // Given
        Long id = 1L;
        float[] vector = MockDataGenerator.randomVector(768);

        // When & Then
        assertDoesNotThrow(() -> vectorStore.addVector(id, vector));
    }

    @Test
    @DisplayName("添加向量 - 批量向量")
    void testAddVectors_Batch() {
        // Given
        Long[] ids = {1L, 2L, 3L, 4L, 5L};
        float[][] vectors = new float[5][];
        for (int i = 0; i < 5; i++) {
            vectors[i] = MockDataGenerator.randomVector(768);
        }

        // When & Then
        assertDoesNotThrow(() -> vectorStore.addVectors(ids, vectors));
    }

    @Test
    @DisplayName("搜索相似向量 - 成功")
    void testSearchSimilar_Success() {
        // Given
        Long id = 1L;
        float[] vector = MockDataGenerator.randomVector(768);
        vectorStore.addVector(id, vector);

        // When
        List<VectorStore.SearchResult> results = vectorStore.searchSimilar(vector, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.size() <= 5);
    }

    @Test
    @DisplayName("搜索相似向量 - 空索引")
    void testSearchSimilar_EmptyIndex() {
        // Given
        float[] queryVector = MockDataGenerator.randomVector(768);

        // When
        List<VectorStore.SearchResult> results = vectorStore.searchSimilar(queryVector, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("删除向量 - 成功")
    void testDeleteVector_Success() {
        // Given
        Long id = 1L;
        float[] vector = MockDataGenerator.randomVector(768);
        vectorStore.addVector(id, vector);

        // When & Then
        assertDoesNotThrow(() -> vectorStore.deleteVector(id));
    }

    @Test
    @DisplayName("保存和加载索引 - 成功")
    void testSaveAndLoadIndex_Success() {
        // Given
        Long id = 1L;
        float[] vector = MockDataGenerator.randomVector(768);
        vectorStore.addVector(id, vector);

        // When
        assertDoesNotThrow(() -> vectorStore.saveIndex());

        // Then
        VectorStore newVectorStore = new VectorStore(
            tempDir.resolve("test_faiss_index").toString(), 
            768
        );
        assertDoesNotThrow(() -> newVectorStore.loadIndex());
    }

    @Test
    @DisplayName("获取索引大小 - 成功")
    void testGetIndexSize_Success() {
        // Given
        for (long i = 1; i <= 10; i++) {
            vectorStore.addVector(i, MockDataGenerator.randomVector(768));
        }

        // When
        long size = vectorStore.getIndexSize();

        // Then
        assertEquals(10, size);
    }

    @Test
    @DisplayName("清空索引 - 成功")
    void testClearIndex_Success() {
        // Given
        vectorStore.addVector(1L, MockDataGenerator.randomVector(768));
        vectorStore.addVector(2L, MockDataGenerator.randomVector(768));

        // When
        vectorStore.clearIndex();

        // Then
        assertEquals(0, vectorStore.getIndexSize());
    }

    @Test
    @DisplayName("搜索结果排序 - 按相似度降序")
    void testSearchResults_OrderedBySimilarity() {
        // Given
        for (long i = 1; i <= 5; i++) {
            vectorStore.addVector(i, MockDataGenerator.randomVector(768));
        }
        float[] queryVector = MockDataGenerator.randomVector(768);

        // When
        List<VectorStore.SearchResult> results = vectorStore.searchSimilar(queryVector, 5);

        // Then
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getSimilarity() >= results.get(i + 1).getSimilarity());
        }
    }
}

