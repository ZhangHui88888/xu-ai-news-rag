package com.xu.news.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试（不依赖Spring）
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("JWT工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testToken;
    private Long testUserId = 1L;
    private String testUsername = "testuser";
    private String testRole = "user";

    @BeforeEach
    void setUp() {
        // 手动创建JwtUtil，提供测试配置
        jwtUtil = new JwtUtil();
        // 通过反射设置private字段
        setField(jwtUtil, "secret", "TEST_SECRET_KEY_FOR_UNIT_TESTING_XU_NEWS_AI_RAG_2025_VERY_LONG_STRING_MINIMUM_256_BITS");
        setField(jwtUtil, "expiration", 3600000L);
        
        testToken = jwtUtil.generateToken(testUserId, testUsername, testRole);
    }

    @Test
    @DisplayName("生成Token")
    void testGenerateToken() {
        // When
        String token = jwtUtil.generateToken(testUserId, testUsername, testRole);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("从Token中获取用户名")
    void testGetUsernameFromToken() {
        // When
        String username = jwtUtil.getUsernameFromToken(testToken);

        // Then
        assertEquals(testUsername, username);
    }

    @Test
    @DisplayName("从Token中获取用户ID")
    void testGetUserIdFromToken() {
        // When
        Long userId = jwtUtil.getUserIdFromToken(testToken);

        // Then
        assertEquals(testUserId, userId);
    }

    @Test
    @DisplayName("验证Token - 有效")
    void testValidateToken_Valid() {
        // When
        Boolean isValid = jwtUtil.validateToken(testToken, testUsername);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证Token - 无效")
    void testValidateToken_Invalid() {
        // When
        Boolean isValid = jwtUtil.validateToken("invalid.token", testUsername);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("检查Token是否过期")
    void testIsTokenExpired() {
        // When
        Boolean isExpired = jwtUtil.isTokenExpired(testToken);

        // Then
        assertFalse(isExpired);
    }

    /**
     * 通过反射设置private字段（用于测试）
     */
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

