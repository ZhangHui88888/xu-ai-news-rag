package com.xu.news.util;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 向量存储工具类（FAISS封装）
 * 
 * 注意：这是简化实现。实际使用FAISS需要：
 * 1. 使用FAISS的Java JNI绑定
 * 2. 或通过Python服务（FastAPI）封装FAISS
 * 3. 或使用向量数据库如Milvus、Weaviate
 * 
 * @author XU
 * @since 2025-10-15
 */
@Slf4j
@Component
public class VectorStore {

    @Value("${faiss.index-path}")
    private String indexPath;

    @Value("${faiss.dimension}")
    private Integer dimension;

    // 简化实现：内存中存储向量（生产环境应使用FAISS）
    private final Map<Long, List<Double>> vectors = new HashMap<>();
    private Long nextVectorId = 1L;

    /**
     * 添加向量
     */
    public Long addVector(List<Double> vector) {
        if (vector.size() != dimension) {
            throw new IllegalArgumentException("向量维度不匹配，期望: " + dimension + ", 实际: " + vector.size());
        }
        
        Long vectorId = nextVectorId++;
        vectors.put(vectorId, vector);
        log.debug("添加向量ID: {}", vectorId);
        return vectorId;
    }

    /**
     * 批量添加向量
     */
    public List<Long> addVectors(List<List<Double>> vectorList) {
        List<Long> ids = new ArrayList<>();
        for (List<Double> vector : vectorList) {
            ids.add(addVector(vector));
        }
        return ids;
    }

    /**
     * 搜索最相似的向量（余弦相似度）
     */
    public List<SearchResult> search(List<Double> queryVector, int topK) {
        if (queryVector.size() != dimension) {
            throw new IllegalArgumentException("查询向量维度不匹配");
        }

        List<SearchResult> results = new ArrayList<>();
        
        for (Map.Entry<Long, List<Double>> entry : vectors.entrySet()) {
            double similarity = cosineSimilarity(queryVector, entry.getValue());
            results.add(new SearchResult(entry.getKey(), similarity));
        }

        // 按相似度降序排序
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 返回Top K
        return results.subList(0, Math.min(topK, results.size()));
    }

    /**
     * 搜索最相似的向量（带阈值过滤）
     */
    public List<SearchResult> searchWithThreshold(List<Double> queryVector, int topK, double threshold) {
        List<SearchResult> results = search(queryVector, topK);
        results.removeIf(result -> result.getScore() < threshold);
        return results;
    }

    /**
     * 删除向量
     */
    public void deleteVector(Long vectorId) {
        vectors.remove(vectorId);
        log.debug("删除向量ID: {}", vectorId);
    }

    /**
     * 批量删除向量
     */
    public void deleteVectors(List<Long> vectorIds) {
        for (Long id : vectorIds) {
            deleteVector(id);
        }
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 持久化向量索引
     */
    public void saveIndex() throws IOException {
        Path indexFilePath = Paths.get(indexPath);
        Files.createDirectories(indexFilePath.getParent());
        
        Map<String, Object> indexData = new HashMap<>();
        indexData.put("vectors", vectors);
        indexData.put("nextVectorId", nextVectorId);
        indexData.put("dimension", dimension);

        try (FileWriter writer = new FileWriter(indexFilePath.toFile())) {
            writer.write(JSON.toJSONString(indexData));
        }
        
        log.info("向量索引已保存到: {}", indexPath);
    }

    /**
     * 加载向量索引
     */
    @SuppressWarnings("unchecked")
    public void loadIndex() throws IOException {
        Path indexFilePath = Paths.get(indexPath);
        
        if (!Files.exists(indexFilePath)) {
            log.info("向量索引文件不存在，将创建新索引");
            return;
        }

        try (FileReader reader = new FileReader(indexFilePath.toFile())) {
            StringBuilder content = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
            
            Map<String, Object> indexData = JSON.parseObject(content.toString(), Map.class);
            
            // 这里需要处理类型转换
            Map<String, Object> rawVectors = (Map<String, Object>) indexData.get("vectors");
            vectors.clear();
            for (Map.Entry<String, Object> entry : rawVectors.entrySet()) {
                Long id = Long.parseLong(entry.getKey());
                List<Double> vector = JSON.parseArray(JSON.toJSONString(entry.getValue()), Double.class);
                vectors.put(id, vector);
            }
            
            nextVectorId = Long.parseLong(indexData.get("nextVectorId").toString());
            
            log.info("向量索引已加载，共 {} 个向量", vectors.size());
        }
    }

    /**
     * 获取向量数量
     */
    public int getVectorCount() {
        return vectors.size();
    }

    /**
     * 搜索结果类
     */
    public static class SearchResult {
        private final Long vectorId;
        private final Double score;

        public SearchResult(Long vectorId, Double score) {
            this.vectorId = vectorId;
            this.score = score;
        }

        public Long getVectorId() {
            return vectorId;
        }

        public Double getScore() {
            return score;
        }
    }
}

