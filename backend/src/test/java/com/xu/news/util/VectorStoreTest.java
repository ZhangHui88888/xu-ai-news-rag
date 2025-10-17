package com.xu.news.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VectorStore 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("向量存储测试")
@Import(com.xu.news.config.TestConfiguration.class)
class VectorStoreTest {

    @Autowired
    private VectorStore vectorStore;

    @Test
    @DisplayName("添加向量 - 成功")
    void testAddVector_Success() {
        // Given
        List<Double> vector = createTestVector(768);

        // When
        Long vectorId = vectorStore.addVector(vector);

        // Then
        assertNotNull(vectorId);
        assertTrue(vectorId > 0);
    }

    @Test
    @DisplayName("添加向量 - 维度不匹配")
    void testAddVector_DimensionMismatch() {
        // Given
        List<Double> vector = createTestVector(512);  // 错误的维度

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStore.addVector(vector);
        });
    }

    @Test
    @DisplayName("搜索向量 - 成功")
    void testSearch_Success() {
        // Given
        List<Double> vector1 = createTestVector(768);
        List<Double> vector2 = createTestVector(768);
        vectorStore.addVector(vector1);
        vectorStore.addVector(vector2);

        // When
        List<VectorStore.SearchResult> results = vectorStore.search(vector1, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        // 第一个结果应该是最相似的（自己）
        assertTrue(results.get(0).getScore() >= 0.99);
    }

    @Test
    @DisplayName("搜索向量 - 限制结果数量")
    void testSearch_LimitResults() {
        // Given
        for (int i = 1; i <= 10; i++) {
            vectorStore.addVector(createTestVector(768));
        }
        List<Double> queryVector = createTestVector(768);

        // When
        List<VectorStore.SearchResult> results = vectorStore.search(queryVector, 3);

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("删除向量 - 成功")
    void testDeleteVector_Success() {
        // Given
        List<Double> vector = createTestVector(768);
        Long vectorId = vectorStore.addVector(vector);

        // When
        vectorStore.deleteVector(vectorId);

        // Then
        // 删除后搜索不应该包含该向量
        List<VectorStore.SearchResult> results = vectorStore.search(vector, 10);
        assertTrue(results.stream().noneMatch(r -> r.getVectorId().equals(vectorId)));
    }

    @Test
    @DisplayName("批量添加向量")
    void testBatchAddVectors() {
        // Given
        List<List<Double>> vectors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            vectors.add(createTestVector(768));
        }
        
        // When
        List<Long> vectorIds = vectorStore.addVectors(vectors);

        // Then
        assertEquals(10, vectorIds.size());
        assertNotNull(vectorIds.get(0));
    }

    @Test
    @DisplayName("向量相似度计算")
    void testVectorSimilarity() {
        // Given
        List<Double> vector1 = createTestVector(768);
        List<Double> vector2 = new ArrayList<>(vector1);  // 相同向量

        // When
        vectorStore.addVector(vector1);
        vectorStore.addVector(vector2);
        List<VectorStore.SearchResult> results = vectorStore.search(vector1, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        // 相同向量的相似度应该最高
        assertTrue(results.get(0).getScore() >= 0.99);
    }

    @Test
    @DisplayName("搜索维度不匹配")
    void testSearch_DimensionMismatch() {
        // Given
        List<Double> wrongVector = createTestVector(512);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStore.search(wrongVector, 5);
        });
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

