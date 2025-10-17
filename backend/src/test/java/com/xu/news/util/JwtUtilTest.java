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
 * @since 2025-10-17
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JWT工具类测试")
@Import(com.xu.news.config.TestConfiguration.class)
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private String testToken;
    private Long testUserId = 1L;
    private String testUsername = "testuser";
    private String testRole = "user";

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
        assertTrue(token.split("\\.").length == 3);  // JWT应该有三部分
    }

    @Test
    @DisplayName("从Token中获取用户名 - 成功")
    void testGetUsernameFromToken_Success() {
        // When
        String username = jwtUtil.getUsernameFromToken(testToken);

        // Then
        assertEquals(testUsername, username);
    }

    @Test
    @DisplayName("从Token中获取用户ID - 成功")
    void testGetUserIdFromToken_Success() {
        // When
        Long userId = jwtUtil.getUserIdFromToken(testToken);

        // Then
        assertEquals(testUserId, userId);
    }

    @Test
    @DisplayName("从Token中获取角色 - 成功")
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
    void testValidateToken_UsernameMismatch() {
        // When
        Boolean isValid = jwtUtil.validateToken(testToken, "wronguser");

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证Token - 无效Token")
    void testValidateToken_InvalidToken() {
        // When
        Boolean isValid = jwtUtil.validateToken("invalid.token.here", testUsername);

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
        // When
        Boolean isExpired = jwtUtil.isTokenExpired("invalid.token.here");

        // Then
        assertTrue(isExpired);
    }

    @Test
    @DisplayName("生成Token - 不同角色")
    void testGenerateToken_DifferentRoles() {
        // Given
        String adminToken = jwtUtil.generateToken(2L, "admin", "admin");
        String userToken = jwtUtil.generateToken(3L, "user", "user");

        // When
        String adminRole = jwtUtil.getRoleFromToken(adminToken);
        String userRole = jwtUtil.getRoleFromToken(userToken);

        // Then
        assertEquals("admin", adminRole);
        assertEquals("user", userRole);
    }

    @Test
    @DisplayName("从Token获取Claims - 成功")
    void testGetClaimsFromToken_Success() {
        // When & Then
        assertDoesNotThrow(() -> {
            var claims = jwtUtil.getClaimsFromToken(testToken);
            assertNotNull(claims);
            assertEquals(testUsername, claims.getSubject());
            assertEquals(testUserId, claims.get("userId", Long.class));
            assertEquals(testRole, claims.get("role", String.class));
        });
    }

    @Test
    @DisplayName("Token包含所有必要信息")
    void testToken_ContainsAllInfo() {
        // When
        String username = jwtUtil.getUsernameFromToken(testToken);
        Long userId = jwtUtil.getUserIdFromToken(testToken);
        String role = jwtUtil.getRoleFromToken(testToken);
        Boolean isExpired = jwtUtil.isTokenExpired(testToken);
        Boolean isValid = jwtUtil.validateToken(testToken, testUsername);

        // Then
        assertEquals(testUsername, username);
        assertEquals(testUserId, userId);
        assertEquals(testRole, role);
        assertFalse(isExpired);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("生成Token - 空值处理")
    void testGenerateToken_WithNullValues() {
        // When & Then - 应该抛出异常或处理空值
        assertThrows(Exception.class, () -> {
            jwtUtil.generateToken(null, null, null);
        });
    }
}

