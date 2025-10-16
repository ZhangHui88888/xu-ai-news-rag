package com.xu.news.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VectorStore 单元测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest(classes = com.xu.news.XuNewsApplication.class)
@ActiveProfiles("test")
@DisplayName("向量存储测试")
class VectorStoreTest {

    @Autowired
    private VectorStore vectorStore;

    private List<Double> testVector;

    @BeforeEach
    void setUp() {
        // 创建768维测试向量
        testVector = new ArrayList<>();
        for (int i = 0; i < 768; i++) {
            testVector.add(Math.random());
        }
    }

    @Test
    @DisplayName("添加单个向量 - 成功")
    void testAddVector_Success() {
        // When
        Long vectorId = vectorStore.addVector(testVector);

        // Then
        assertNotNull(vectorId);
        assertTrue(vectorId > 0);
    }

    @Test
    @DisplayName("添加向量 - 维度不匹配")
    void testAddVector_DimensionMismatch() {
        // Given
        List<Double> wrongVector = Arrays.asList(0.1, 0.2, 0.3); // 只有3维

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStore.addVector(wrongVector);
        });
    }

    @Test
    @DisplayName("批量添加向量 - 成功")
    void testAddVectors_Success() {
        // Given
        List<List<Double>> vectors = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Double> vector = new ArrayList<>();
            for (int j = 0; j < 768; j++) {
                vector.add(Math.random());
            }
            vectors.add(vector);
        }

        // When
        List<Long> ids = vectorStore.addVectors(vectors);

        // Then
        assertNotNull(ids);
        assertEquals(5, ids.size());
        for (Long id : ids) {
            assertNotNull(id);
            assertTrue(id > 0);
        }
    }

    @Test
    @DisplayName("搜索相似向量 - 成功")
    void testSearch_Success() {
        // Given
        Long id1 = vectorStore.addVector(testVector);
        
        // When
        List<VectorStore.SearchResult> results = vectorStore.search(testVector, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals(id1, results.get(0).getVectorId());
        // 相同向量的相似度应该接近1.0
        assertTrue(results.get(0).getScore() > 0.99);
    }

    @Test
    @DisplayName("搜索相似向量 - TopK限制")
    void testSearch_TopKLimit() {
        // Given
        for (int i = 0; i < 10; i++) {
            List<Double> vector = new ArrayList<>();
            for (int j = 0; j < 768; j++) {
                vector.add(Math.random());
            }
            vectorStore.addVector(vector);
        }

        // When
        List<VectorStore.SearchResult> results = vectorStore.search(testVector, 5);

        // Then
        assertNotNull(results);
        assertEquals(5, results.size());
    }

    @Test
    @DisplayName("搜索相似向量 - 按相似度排序")
    void testSearch_OrderedByScore() {
        // Given
        for (int i = 0; i < 5; i++) {
            List<Double> vector = new ArrayList<>();
            for (int j = 0; j < 768; j++) {
                vector.add(Math.random());
            }
            vectorStore.addVector(vector);
        }

        // When
        List<VectorStore.SearchResult> results = vectorStore.search(testVector, 10);

        // Then
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getScore() >= results.get(i + 1).getScore());
        }
    }

    @Test
    @DisplayName("删除向量 - 成功")
    void testDeleteVector_Success() {
        // Given
        Long vectorId = vectorStore.addVector(testVector);
        int countBefore = vectorStore.getVectorCount();

        // When
        vectorStore.deleteVector(vectorId);

        // Then
        assertEquals(countBefore - 1, vectorStore.getVectorCount());
    }

    @Test
    @DisplayName("批量删除向量 - 成功")
    void testDeleteVectors_Success() {
        // Given
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<Double> vector = new ArrayList<>();
            for (int j = 0; j < 768; j++) {
                vector.add(Math.random());
            }
            ids.add(vectorStore.addVector(vector));
        }
        int countBefore = vectorStore.getVectorCount();

        // When
        vectorStore.deleteVectors(ids);

        // Then
        assertEquals(countBefore - 3, vectorStore.getVectorCount());
    }

    @Test
    @DisplayName("获取向量数量 - 成功")
    void testGetVectorCount_Success() {
        // Given
        int initialCount = vectorStore.getVectorCount();
        
        // When
        vectorStore.addVector(testVector);
        vectorStore.addVector(testVector);

        // Then
        assertEquals(initialCount + 2, vectorStore.getVectorCount());
    }

    @Test
    @DisplayName("带阈值搜索 - 成功")
    void testSearchWithThreshold_Success() {
        // Given
        vectorStore.addVector(testVector);

        // When
        List<VectorStore.SearchResult> results = vectorStore.searchWithThreshold(testVector, 5, 0.9);

        // Then
        assertNotNull(results);
        for (VectorStore.SearchResult result : results) {
            assertTrue(result.getScore() >= 0.9);
        }
    }
}

