package com.xu.news.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Ollama客户端工具类
 * 
 * @author XU
 * @since 2025-10-15
 */
@Component
public class OllamaClient {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model.llm}")
    private String llmModel;

    @Value("${ollama.model.embedding}")
    private String embeddingModel;

    @Value("${ollama.timeout}")
    private Long timeout;

    private final OkHttpClient client;
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    public OllamaClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 生成文本回答
     */
    public String generateAnswer(String prompt) throws IOException {
        return generateAnswer(prompt, null);
    }

    /**
     * 生成文本回答（带上下文）
     */
    public String generateAnswer(String prompt, List<String> context) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", llmModel);
        requestBody.put("prompt", buildPrompt(prompt, context));
        requestBody.put("stream", false);

        String jsonBody = JSON.toJSONString(requestBody);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/generate")
                .post(RequestBody.create(jsonBody, JSON_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama API调用失败: " + response.code());
            }
            String responseBody = response.body().string();
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            return jsonResponse.getString("response");
        }
    }

    /**
     * 生成文本嵌入向量
     */
    public List<Double> generateEmbedding(String text) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", embeddingModel);
        requestBody.put("prompt", text);

        String jsonBody = JSON.toJSONString(requestBody);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/embeddings")
                .post(RequestBody.create(jsonBody, JSON_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama Embedding API调用失败: " + response.code());
            }
            String responseBody = response.body().string();
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            return jsonResponse.getList("embedding", Double.class);
        }
    }

    /**
     * 生成摘要
     */
    public String generateSummary(String content) throws IOException {
        String prompt = "请为以下内容生成一个简洁的中文摘要（不超过200字）：\n\n" + content;
        return generateAnswer(prompt);
    }

    /**
     * 构建RAG提示词
     */
    private String buildPrompt(String question, List<String> context) {
        if (context == null || context.isEmpty()) {
            return question;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("根据以下参考资料回答问题：\n\n");
        prompt.append("参考资料：\n");
        for (int i = 0; i < context.size(); i++) {
            prompt.append(i + 1).append(". ").append(context.get(i)).append("\n\n");
        }
        prompt.append("问题：").append(question).append("\n\n");
        prompt.append("请基于以上参考资料提供准确、详细的回答。如果参考资料中没有相关信息，请说明。");
        
        return prompt.toString();
    }
}

