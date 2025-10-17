package com.xu.news.integration;

import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.entity.User;
import com.xu.news.service.UserService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户认证集成测试（使用Mock）
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户认证集成测试")
class UserAuthIntegrationTest {

    @Mock
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
    @DisplayName("完整认证流程 - 注册后登录")
    void testCompleteAuthFlow() {
        // Step 1: 注册
        when(userService.register(any(RegisterRequest.class))).thenReturn(testUser);
        User registered = userService.register(registerRequest);
        assertNotNull(registered);

        // Step 2: 登录
        when(userService.login(any(LoginRequest.class))).thenReturn("test-token");
        String token = userService.login(loginRequest);
        assertNotNull(token);

        verify(userService, times(1)).register(any(RegisterRequest.class));
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("注册失败 - 用户名重复")
    void testRegister_UsernameDuplicate() {
        // Given
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("用户名已存在"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("用户名或密码错误"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
    }
}

