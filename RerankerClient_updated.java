package com.xu.news.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 重排模型客户端
 * 使用 Cherry Studio / SiliconFlow API
 * 
 * @author XU
 * @since 2025-10-16
 */
@Slf4j
@Component
public class RerankerClient {

    @Value("${reranker.base-url}")
    private String baseUrl;

    @Value("${reranker.api-key}")
    private String apiKey;

    @Value("${reranker.model}")
    private String model;

    @Value("${reranker.enabled:false}")
    private Boolean enabled;

    private final OkHttpClient client;
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    public RerankerClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Bean 初始化后调用，打印配置信息
     */
    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("🔧 重排模型客户端初始化");
        log.info("启用状态: {}", enabled);
        log.info("API地址: {}", baseUrl);
        log.info("模型名称: {}", model);
        log.info("API Key: {}...", apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "未配置");
        if (enabled != null && enabled) {
            log.info("✅ 重排模型已启用: {}", model);
        } else {
            log.warn("⚠️  重排模型未启用");
        }
        log.info("========================================");
    }

    /**
     * 对检索结果进行重排序
     * 
     * @param query 用户查询
     * @param documents 候选文档列表
     * @param topK 返回前 K 个结果
     * @return 重排后的结果列表
     */
    public List<RerankResult> rerank(String query, List<String> documents, int topK) throws IOException {
        if (!enabled) {
            log.warn("重排模型未启用，跳过重排");
            return getDefaultResults(documents.size(), topK);
        }

        if (documents.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建请求
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("query", query);
        requestBody.put("documents", documents);
        requestBody.put("top_n", topK);
        requestBody.put("return_documents", false);

        String jsonBody = JSON.toJSONString(requestBody);
        log.info("🔄 执行重排: query='{}', documents={}, topK={}", query, documents.size(), topK);

        Request request = new Request.Builder()
                .url(baseUrl + "/v1/rerank")
                .post(RequestBody.create(jsonBody, JSON_TYPE))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                log.error("重排API调用失败: code={}, body={}", response.code(), errorBody);
                throw new IOException("重排API调用失败: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            log.debug("重排响应: {}", responseBody);

            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray results = jsonResponse.getJSONArray("results");

            List<RerankResult> rerankResults = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                JSONObject result = results.getJSONObject(i);
                RerankResult rerankResult = new RerankResult();
                rerankResult.setIndex(result.getInteger("index"));
                rerankResult.setRelevanceScore(result.getDouble("relevance_score"));
                rerankResults.add(rerankResult);
            }

            log.info("✅ 重排完成: 输入文档数={}, 输出结果数={}", documents.size(), rerankResults.size());
            return rerankResults;
        }
    }

    /**
     * 检查重排模型是否可用
     */
    public boolean isAvailable() {
        if (!enabled) {
            return false;
        }
        
        try {
            // 简单测试
            List<String> testDocs = List.of("测试文档1", "测试文档2");
            rerank("测试查询", testDocs, 1);
            return true;
        } catch (Exception e) {
            log.warn("重排模型不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 当重排不可用时返回默认结果
     */
    private List<RerankResult> getDefaultResults(int totalDocs, int topK) {
        List<RerankResult> results = new ArrayList<>();
        int count = Math.min(totalDocs, topK);
        for (int i = 0; i < count; i++) {
            RerankResult result = new RerankResult();
            result.setIndex(i);
            result.setRelevanceScore(1.0 - (i * 0.1)); // 模拟递减分数
            results.add(result);
        }
        return results;
    }

    /**
     * 重排结果类
     */
    @Data
    public static class RerankResult {
        /**
         * 文档在原列表中的索引
         */
        private Integer index;
        
        /**
         * 相关性分数（越高越相关）
         */
        private Double relevanceScore;
    }
}

