package com.xu.news.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
     * 生成标签（AI自动分析内容并提取关键词作为标签）
     * 
     * @param title 标题
     * @param content 内容
     * @return 标签的JSON数组字符串，如：["AI", "技术", "教程"]
     */
    public String generateTags(String title, String content) throws IOException {
        // 限制内容长度，避免token过多
        String limitedContent = content.length() > 1000 ? content.substring(0, 1000) : content;
        
        String prompt = String.format(
            "请分析以下文档的标题和内容，提取3-5个最相关的关键词作为标签。\n\n" +
            "标题：%s\n\n" +
            "内容：%s\n\n" +
            "要求：\n" +
            "1. 只返回标签，用逗号分隔\n" +
            "2. 标签应该简洁（2-4个字）\n" +
            "3. 优先选择技术术语、领域关键词\n" +
            "4. 不要返回任何解释，只返回标签\n" +
            "5. 例如：人工智能,机器学习,深度学习,神经网络\n\n" +
            "标签：",
            title, limitedContent
        );
        
        try {
            String response = generateAnswer(prompt).trim();
            
            // 解析返回的标签字符串，转换为JSON数组
            String[] tagArray = response.split("[,，、]");  // 支持中英文逗号和顿号
            java.util.List<String> tags = new java.util.ArrayList<>();
            
            for (String tag : tagArray) {
                String cleanTag = tag.trim();
                // 过滤掉过长或过短的标签
                if (cleanTag.length() >= 2 && cleanTag.length() <= 10) {
                    tags.add(cleanTag);
                }
            }
            
            // 最多保留5个标签
            if (tags.size() > 5) {
                tags = tags.subList(0, 5);
            }
            
            // 转换为JSON数组字符串
            return JSON.toJSONString(tags);
            
        } catch (Exception e) {
            // 如果生成失败，返回默认标签
            log.warn("生成标签失败: {}", e.getMessage());
            return JSON.toJSONString(java.util.Arrays.asList("文档"));
        }
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

