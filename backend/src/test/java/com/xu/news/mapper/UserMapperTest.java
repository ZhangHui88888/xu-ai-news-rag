package com.xu.news.mapper;

import com.xu.news.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper 集成测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest(classes = com.xu.news.XuNewsApplication.class)
@ActiveProfiles("test")
@Transactional
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("用户Mapper测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("newuser" + System.currentTimeMillis());
        testUser.setPasswordHash("$2a$10$testHash");
        testUser.setEmail("newuser" + System.currentTimeMillis() + "@test.com");
        testUser.setFullName("新测试用户");
        testUser.setRole("user");
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("插入用户 - 成功")
    void testInsert_Success() {
        // When
        int result = userMapper.insert(testUser);

        // Then
        assertEquals(1, result);
        assertNotNull(testUser.getId());
        assertTrue(testUser.getId() > 0);
    }

    @Test
    @DisplayName("根据用户名查询 - 成功")
    void testFindByUsername_Success() {
        // Given
        userMapper.insert(testUser);

        // When
        User found = userMapper.findByUsername(testUser.getUsername());

        // Then
        assertNotNull(found);
        assertEquals(testUser.getUsername(), found.getUsername());
        assertEquals(testUser.getEmail(), found.getEmail());
    }

    @Test
    @DisplayName("根据用户名查询 - 不存在")
    void testFindByUsername_NotFound() {
        // When
        User found = userMapper.findByUsername("nonexistent_user_12345");

        // Then
        assertNull(found);
    }

    @Test
    @DisplayName("根据邮箱查询 - 成功")
    void testFindByEmail_Success() {
        // Given
        userMapper.insert(testUser);

        // When
        User found = userMapper.findByEmail(testUser.getEmail());

        // Then
        assertNotNull(found);
        assertEquals(testUser.getEmail(), found.getEmail());
    }

    @Test
    @DisplayName("根据ID查询 - 成功")
    void testSelectById_Success() {
        // Given
        userMapper.insert(testUser);
        Long userId = testUser.getId();

        // When
        User found = userMapper.selectById(userId);

        // Then
        assertNotNull(found);
        assertEquals(userId, found.getId());
    }

    @Test
    @DisplayName("更新用户 - 成功")
    void testUpdateById_Success() {
        // Given
        userMapper.insert(testUser);
        testUser.setFullName("更新后的名字");
        testUser.setLastLoginAt(LocalDateTime.now());

        // When
        int result = userMapper.updateById(testUser);

        // Then
        assertEquals(1, result);
        User updated = userMapper.selectById(testUser.getId());
        assertEquals("更新后的名字", updated.getFullName());
    }

    @Test
    @DisplayName("删除用户 - 逻辑删除")
    void testDeleteById_LogicalDelete() {
        // Given
        userMapper.insert(testUser);
        Long userId = testUser.getId();

        // When
        int result = userMapper.deleteById(userId);

        // Then
        assertEquals(1, result);
        // 逻辑删除后查询应该返回null
        User deleted = userMapper.selectById(userId);
        assertNull(deleted);
    }
}

