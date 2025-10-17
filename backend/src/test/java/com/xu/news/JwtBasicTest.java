package com.xu.news;

import com.xu.news.config.TestConfiguration;
import com.xu.news.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单JWT测试 - 验证基础功能
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest(classes = com.xu.news.XuNewsApplication.class)
@ActiveProfiles("test")
@DisplayName("简单JWT测试")
@Import(TestConfiguration.class)
public class JwtBasicTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("JWT基础功能测试")
    void testJwtBasicFunctions() {
        // 生成Token
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 解析Token
        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("testuser", username);

        Long userId = jwtUtil.getUserIdFromToken(token);
        assertEquals(1L, userId);

        // 验证Token
        Boolean isValid = jwtUtil.validateToken(token, "testuser");
        assertTrue(isValid);

        // 检查是否过期
        Boolean isExpired = jwtUtil.isTokenExpired(token);
        assertFalse(isExpired);
    }
}

