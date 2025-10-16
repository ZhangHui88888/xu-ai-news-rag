package com.xu.news.service;

import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.entity.User;
import com.xu.news.mapper.UserMapper;
import com.xu.news.service.impl.UserServiceImpl;
import com.xu.news.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("Test123456");
        registerRequest.setConfirmPassword("Test123456");
        registerRequest.setEmail("test@example.com");
        registerRequest.setFullName("测试用户");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Test123456");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("$2a$10$encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setRole("user");
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void testRegister_Success() {
        // Given
        when(userMapper.findByUsername(anyString())).thenReturn(null);
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        User result = userService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals(registerRequest.getUsername(), result.getUsername());
        assertEquals(registerRequest.getEmail(), result.getEmail());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 密码不一致")
    void testRegister_PasswordMismatch() {
        // Given
        registerRequest.setConfirmPassword("DifferentPassword");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void testRegister_UsernameExists() {
        // Given
        when(userMapper.findByUsername(registerRequest.getUsername())).thenReturn(testUser);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
        assertTrue(exception.getMessage().contains("用户名已存在"));
    }

    @Test
    @DisplayName("用户注册 - 邮箱已存在")
    void testRegister_EmailExists() {
        // Given
        when(userMapper.findByUsername(anyString())).thenReturn(null);
        when(userMapper.findByEmail(registerRequest.getEmail())).thenReturn(testUser);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
        assertTrue(exception.getMessage().contains("邮箱已被注册"));
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() {
        // Given
        when(userMapper.findByUsername(loginRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-token");
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        String token = userService.login(loginRequest);

        // Then
        assertNotNull(token);
        assertEquals("test-token", token);
        verify(jwtUtil, times(1)).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        // Given
        when(userMapper.findByUsername(anyString())).thenReturn(null);
        when(userMapper.findByEmail(anyString())).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        when(userMapper.findByUsername(loginRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("用户登录 - 账户被禁用")
    void testLogin_AccountDisabled() {
        // Given
        testUser.setStatus(0);
        when(userMapper.findByUsername(loginRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        assertTrue(exception.getMessage().contains("账户已被禁用"));
    }

    @Test
    @DisplayName("根据用户名查询 - 成功")
    void testFindByUsername_Success() {
        // Given
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        // When
        User result = userService.findByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("根据邮箱查询 - 成功")
    void testFindByEmail_Success() {
        // Given
        when(userMapper.findByEmail("test@example.com")).thenReturn(testUser);

        // When
        User result = userService.findByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }
}

