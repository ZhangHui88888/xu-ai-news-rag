package com.xu.news.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VectorStore 单元测试（不依赖Spring）
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("向量存储测试")
class VectorStoreTest {

    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        vectorStore = new VectorStore();
        // 通过反射设置字段
        setField(vectorStore, "indexPath", "./test-data/faiss");
        setField(vectorStore, "dimension", 768);
    }

    @Test
    @DisplayName("添加向量")
    void testAddVector() {
        // Given
        List<Double> vector = createTestVector(768);

        // When
        Long id = vectorStore.addVector(vector);

        // Then
        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    @DisplayName("搜索向量")
    void testSearch() {
        // Given
        List<Double> vector = createTestVector(768);
        vectorStore.addVector(vector);

        // When
        List<VectorStore.SearchResult> results = vectorStore.search(vector, 5);

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    @DisplayName("删除向量")
    void testDeleteVector() {
        // Given
        List<Double> vector = createTestVector(768);
        Long id = vectorStore.addVector(vector);

        // When
        vectorStore.deleteVector(id);

        // Then - 删除后搜索不应包含
        List<VectorStore.SearchResult> results = vectorStore.search(vector, 10);
        assertTrue(results.stream().noneMatch(r -> r.getVectorId().equals(id)));
    }

    @Test
    @DisplayName("维度不匹配")
    void testDimensionMismatch() {
        // Given
        List<Double> wrongVector = createTestVector(512);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStore.addVector(wrongVector);
        });
    }

    private List<Double> createTestVector(int dimension) {
        return IntStream.range(0, dimension)
                .mapToDouble(i -> Math.random())
                .boxed()
                .collect(Collectors.toList());
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}

