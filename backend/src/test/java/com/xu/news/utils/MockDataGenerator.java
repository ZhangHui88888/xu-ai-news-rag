package com.xu.news.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mock数据生成器
 * 
 * @author XU
 * @since 2025-10-16
 */
public class MockDataGenerator {

    private static final Random random = new Random();

    /**
     * 生成随机字符串
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机邮箱
     */
    public static String randomEmail() {
        return randomString(10) + "@test.com";
    }

    /**
     * 生成随机向量（模拟FAISS向量）
     */
    public static float[] randomVector(int dimension) {
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = random.nextFloat();
        }
        return vector;
    }

    /**
     * 生成随机整数
     */
    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * 生成随机长整数
     */
    public static long randomLong() {
        return random.nextLong();
    }

    /**
     * 生成模拟的Ollama响应
     */
    public static String mockOllamaResponse(String prompt) {
        return "这是对\"" + prompt + "\"的模拟AI回答。在实际测试中，这会是Ollama模型生成的回答。";
    }

    /**
     * 生成模拟的向量检索结果
     */
    public static List<Long> mockVectorSearchResults(int count) {
        List<Long> results = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            results.add((long) i);
        }
        return results;
    }
}

