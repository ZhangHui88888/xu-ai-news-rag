package com.xu.news.mapper;

import com.xu.news.entity.User;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper 集成测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("用户Mapper测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
    }

    @Test
    @DisplayName("插入用户 - 成功")
    void testInsert_Success() {
        // When
        int result = userMapper.insert(testUser);

        // Then
        assertEquals(1, result);
        assertNotNull(testUser.getId());
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
        User found = userMapper.findByUsername("nonexistent");

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

        // When
        User found = userMapper.selectById(testUser.getId());

        // Then
        assertNotNull(found);
        assertEquals(testUser.getId(), found.getId());
    }

    @Test
    @DisplayName("更新用户 - 成功")
    void testUpdateById_Success() {
        // Given
        userMapper.insert(testUser);
        testUser.setFullName("Updated Name");

        // When
        int result = userMapper.updateById(testUser);

        // Then
        assertEquals(1, result);
        
        User updated = userMapper.selectById(testUser.getId());
        assertEquals("Updated Name", updated.getFullName());
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void testDeleteById_Success() {
        // Given
        userMapper.insert(testUser);
        Long userId = testUser.getId();

        // When
        int result = userMapper.deleteById(userId);

        // Then
        assertEquals(1, result);
        
        User deleted = userMapper.selectById(userId);
        assertNull(deleted);
    }

    @Test
    @DisplayName("根据邮箱查询 - 不存在")
    void testFindByEmail_NotFound() {
        // When
        User found = userMapper.findByEmail("nonexistent@example.com");

        // Then
        assertNull(found);
    }
}

