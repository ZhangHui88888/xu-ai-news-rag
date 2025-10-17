package com.xu.news.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VectorStore 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("向量存储测试")
class VectorStoreTest {

    private VectorStore vectorStore;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // 使用临时目录初始化向量存储
        vectorStore = new VectorStore(tempDir.toString(), 768);
    }

    @Test
    @DisplayName("添加向量 - 成功")
    void testAddVector_Success() {
        // Given
        Long id = 1L;
        float[] vector = createTestVector(768);

        // When
        boolean result = vectorStore.addVector(id, vector);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("添加向量 - 维度不匹配")
    void testAddVector_DimensionMismatch() {
        // Given
        Long id = 1L;
        float[] vector = createTestVector(512);  // 错误的维度

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            vectorStore.addVector(id, vector);
        });
    }

    @Test
    @DisplayName("添加向量 - 空向量")
    void testAddVector_NullVector() {
        // Given
        Long id = 1L;

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            vectorStore.addVector(id, null);
        });
    }

    @Test
    @DisplayName("搜索向量 - 成功")
    void testSearch_Success() throws IOException {
        // Given
        float[] vector1 = createTestVector(768);
        float[] vector2 = createTestVector(768);
        vectorStore.addVector(1L, vector1);
        vectorStore.addVector(2L, vector2);

        // When
        List<VectorStore.SearchResult> results = vectorStore.search(vector1, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        // 第一个结果应该是最相似的
        assertTrue(results.get(0).getScore() > 0);
    }

    @Test
    @DisplayName("搜索向量 - 空索引")
    void testSearch_EmptyIndex() throws IOException {
        // Given
        float[] vector = createTestVector(768);

        // When
        List<VectorStore.SearchResult> results = vectorStore.search(vector, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("搜索向量 - 限制结果数量")
    void testSearch_LimitResults() throws IOException {
        // Given
        for (int i = 1; i <= 10; i++) {
            vectorStore.addVector((long) i, createTestVector(768));
        }
        float[] queryVector = createTestVector(768);

        // When
        List<VectorStore.SearchResult> results = vectorStore.search(queryVector, 3);

        // Then
        assertNotNull(results);
        assertTrue(results.size() <= 3);
    }

    @Test
    @DisplayName("删除向量 - 成功")
    void testDeleteVector_Success() {
        // Given
        Long id = 1L;
        float[] vector = createTestVector(768);
        vectorStore.addVector(id, vector);

        // When
        boolean result = vectorStore.deleteVector(id);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("删除向量 - 不存在的ID")
    void testDeleteVector_NonExistentId() {
        // When
        boolean result = vectorStore.deleteVector(999L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("批量添加向量")
    void testBatchAddVectors() {
        // Given
        int batchSize = 100;
        
        // When
        for (int i = 1; i <= batchSize; i++) {
            vectorStore.addVector((long) i, createTestVector(768));
        }

        // Then
        // 所有向量应该都添加成功
        assertEquals(batchSize, vectorStore.getVectorCount());
    }

    @Test
    @DisplayName("保存和加载索引")
    void testSaveAndLoadIndex() throws IOException {
        // Given
        vectorStore.addVector(1L, createTestVector(768));
        vectorStore.addVector(2L, createTestVector(768));
        
        // When
        vectorStore.saveIndex();
        VectorStore newVectorStore = new VectorStore(tempDir.toString(), 768);
        newVectorStore.loadIndex();

        // Then
        assertEquals(vectorStore.getVectorCount(), newVectorStore.getVectorCount());
    }

    @Test
    @DisplayName("向量相似度计算")
    void testVectorSimilarity() {
        // Given
        float[] vector1 = createTestVector(768);
        float[] vector2 = Arrays.copyOf(vector1, vector1.length);  // 相同向量

        // When
        vectorStore.addVector(1L, vector1);
        vectorStore.addVector(2L, vector2);
        List<VectorStore.SearchResult> results = vectorStore.search(vector1, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        // 相同向量的相似度应该最高
        assertTrue(results.get(0).getScore() > 0.99f);
    }

    @Test
    @DisplayName("获取向量数量")
    void testGetVectorCount() {
        // Given
        assertEquals(0, vectorStore.getVectorCount());
        
        // When
        vectorStore.addVector(1L, createTestVector(768));
        vectorStore.addVector(2L, createTestVector(768));
        vectorStore.addVector(3L, createTestVector(768));

        // Then
        assertEquals(3, vectorStore.getVectorCount());
    }

    @Test
    @DisplayName("清空向量存储")
    void testClearVectors() {
        // Given
        vectorStore.addVector(1L, createTestVector(768));
        vectorStore.addVector(2L, createTestVector(768));
        assertEquals(2, vectorStore.getVectorCount());

        // When
        vectorStore.clear();

        // Then
        assertEquals(0, vectorStore.getVectorCount());
    }

    /**
     * 创建测试用的向量
     */
    private float[] createTestVector(int dimension) {
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) Math.random();
        }
        return vector;
    }
}

