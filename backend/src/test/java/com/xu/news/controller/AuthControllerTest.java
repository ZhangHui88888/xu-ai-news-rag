package com.xu.news.controller;

import com.xu.news.base.BaseControllerTest;
import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.entity.User;
import com.xu.news.service.UserService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController API测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("认证控制器API测试")
class AuthControllerTest extends BaseControllerTest {

    @MockBean
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = TestDataBuilder.createRegisterRequest();
        loginRequest = TestDataBuilder.createLoginRequest();
        testUser = TestDataBuilder.createTestUser();
    }

    @Test
    @DisplayName("POST /auth/register - 注册成功")
    void testRegister_Success() throws Exception {
        // Given
        when(userService.register(any(RegisterRequest.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /auth/register - 用户名已存在")
    void testRegister_UsernameExists() throws Exception {
        // Given
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("用户名已存在"));

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名已存在"));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /auth/register - 参数校验失败")
    void testRegister_ValidationFailed() throws Exception {
        // Given
        registerRequest.setUsername("");  // 空用户名

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - 登录成功")
    void testLogin_Success() throws Exception {
        // Given
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        when(userService.login(any(LoginRequest.class))).thenReturn(mockToken);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.token").value(mockToken))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - 用户名或密码错误")
    void testLogin_InvalidCredentials() throws Exception {
        // Given
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("用户名或密码错误"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - 账户被禁用")
    void testLogin_AccountDisabled() throws Exception {
        // Given
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("账户已被禁用"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账户已被禁用"));
    }

    @Test
    @DisplayName("GET /auth/health - 健康检查")
    void testHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("XU-News-AI-RAG is running!"));
    }

    @Test
    @DisplayName("POST /auth/login - 参数校验失败")
    void testLogin_ValidationFailed() throws Exception {
        // Given
        loginRequest.setUsername("");  // 空用户名

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/register - 密码不一致")
    void testRegister_PasswordMismatch() throws Exception {
        // Given
        registerRequest.setConfirmPassword("DifferentPassword");
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("两次输入的密码不一致"));

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("两次输入的密码不一致"));
    }
}

