package com.xu.news.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller测试基类 - 提供MockMvc支持
 * 
 * @author XU
 * @since 2025-10-17
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtil jwtUtil;

    protected String authToken;

    @BeforeEach
    void setUpAuth() {
        // 生成测试用的JWT Token
        authToken = jwtUtil.generateToken(1L, "testuser", "user");
    }

    /**
     * 生成管理员Token
     */
    protected String generateAdminToken() {
        return jwtUtil.generateToken(2L, "admin", "admin");
    }

    /**
     * 将对象转换为JSON字符串
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}

