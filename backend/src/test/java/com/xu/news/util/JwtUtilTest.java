package com.xu.news.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JWT工具类测试")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private String testToken;
    private final Long testUserId = 1L;
    private final String testUsername = "testuser";
    private final String testRole = "USER";

    @BeforeEach
    void setUp() {
        testToken = jwtUtil.generateToken(testUserId, testUsername, testRole);
    }

    @Test
    @DisplayName("生成Token - 成功")
    void testGenerateToken_Success() {
        // When
        String token = jwtUtil.generateToken(testUserId, testUsername, testRole);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT应该有3个部分
    }

    @Test
    @DisplayName("从Token获取用户名 - 成功")
    void testGetUsernameFromToken_Success() {
        // When
        String username = jwtUtil.getUsernameFromToken(testToken);

        // Then
        assertEquals(testUsername, username);
    }

    @Test
    @DisplayName("从Token获取用户ID - 成功")
    void testGetUserIdFromToken_Success() {
        // When
        Long userId = jwtUtil.getUserIdFromToken(testToken);

        // Then
        assertEquals(testUserId, userId);
    }

    @Test
    @DisplayName("从Token获取角色 - 成功")
    void testGetRoleFromToken_Success() {
        // When
        String role = jwtUtil.getRoleFromToken(testToken);

        // Then
        assertEquals(testRole, role);
    }

    @Test
    @DisplayName("验证Token - 有效Token")
    void testValidateToken_ValidToken() {
        // When
        Boolean isValid = jwtUtil.validateToken(testToken, testUsername);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证Token - 用户名不匹配")
    void testValidateToken_UsernameNotMatch() {
        // When
        Boolean isValid = jwtUtil.validateToken(testToken, "wronguser");

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证Token - 无效Token")
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken, testUsername);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("检查Token是否过期 - 未过期")
    void testIsTokenExpired_NotExpired() {
        // When
        Boolean isExpired = jwtUtil.isTokenExpired(testToken);

        // Then
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("检查Token是否过期 - 无效Token")
    void testIsTokenExpired_InvalidToken() {
        // Given
        String invalidToken = "invalid.token";

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(invalidToken);

        // Then
        assertTrue(isExpired); // 无效token应该被视为过期
    }

    @Test
    @DisplayName("从Token获取Claims - 成功")
    void testGetClaimsFromToken_Success() {
        // When
        var claims = jwtUtil.getClaimsFromToken(testToken);

        // Then
        assertNotNull(claims);
        assertEquals(testUsername, claims.getSubject());
        assertEquals(testUserId, claims.get("userId", Long.class));
        assertEquals(testRole, claims.get("role", String.class));
    }

    @Test
    @DisplayName("生成Token - 不同参数")
    void testGenerateToken_DifferentParameters() {
        // Given
        Long userId = 999L;
        String username = "differentuser";
        String role = "ADMIN";

        // When
        String token = jwtUtil.generateToken(userId, username, role);

        // Then
        assertNotNull(token);
        assertEquals(username, jwtUtil.getUsernameFromToken(token));
        assertEquals(userId, jwtUtil.getUserIdFromToken(token));
        assertEquals(role, jwtUtil.getRoleFromToken(token));
    }
}

