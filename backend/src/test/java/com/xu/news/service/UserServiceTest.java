package com.xu.news.service;

import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.entity.User;
import com.xu.news.mapper.UserMapper;
import com.xu.news.service.impl.UserServiceImpl;
import com.xu.news.util.JwtUtil;
import com.xu.news.utils.TestDataBuilder;
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

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        registerRequest = TestDataBuilder.createRegisterRequest();
        loginRequest = TestDataBuilder.createLoginRequest();
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
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void testRegister_UsernameExists() {
        // Given
        when(userMapper.findByUsername(registerRequest.getUsername())).thenReturn(testUser);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 邮箱已存在")
    void testRegister_EmailExists() {
        // Given
        when(userMapper.findByUsername(anyString())).thenReturn(null);
        when(userMapper.findByEmail(registerRequest.getEmail())).thenReturn(testUser);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() {
        // Given
        when(userMapper.findByUsername(loginRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-token");

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
        when(userMapper.findByUsername(loginRequest.getUsername())).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        when(userMapper.findByUsername(loginRequest.getUsername())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("根据用户名查询用户 - 成功")
    void testFindByUsername_Success() {
        // Given
        when(userMapper.findByUsername(testUser.getUsername())).thenReturn(testUser);

        // When
        User result = userService.findByUsername(testUser.getUsername());

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userMapper, times(1)).findByUsername(testUser.getUsername());
    }

    @Test
    @DisplayName("根据用户名查询用户 - 不存在")
    void testFindByUsername_NotFound() {
        // Given
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        // When
        User result = userService.findByUsername("nonexistent");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("根据邮箱查询用户 - 成功")
    void testFindByEmail_Success() {
        // Given
        when(userMapper.findByEmail(testUser.getEmail())).thenReturn(testUser);

        // When
        User result = userService.findByEmail(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userMapper, times(1)).findByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("更新最后登录时间 - 成功")
    void testUpdateLastLoginTime_Success() {
        // Given
        Long userId = 1L;
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(userMapper.selectById(userId)).thenReturn(testUser);

        // When
        userService.updateLastLoginTime(userId);

        // Then
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 密码加密")
    void testRegister_PasswordEncrypted() {
        // Given
        String rawPassword = registerRequest.getPassword();
        String encodedPassword = "encrypted_password_hash";
        
        when(userMapper.findByUsername(anyString())).thenReturn(null);
        when(userMapper.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        userService.register(registerRequest);

        // Then
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("用户登录 - 账号被禁用")
    void testLogin_UserDisabled() {
        // Given
        testUser.setStatus(0); // 禁用状态
        when(userMapper.findByUsername(loginRequest.getUsername())).thenReturn(testUser);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });
    }
}

