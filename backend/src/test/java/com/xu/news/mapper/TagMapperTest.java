package com.xu.news.mapper;

import com.xu.news.entity.Tag;
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
 * TagMapper 单元测试（使用Mock）
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("标签Mapper测试")
class TagMapperTest {

    @Mock
    private TagMapper tagMapper;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = TestDataBuilder.createTag();
    }

    @Test
    @DisplayName("插入标签")
    void testInsert() {
        // Given
        when(tagMapper.insert(any(Tag.class))).thenReturn(1);

        // When
        int result = tagMapper.insert(testTag);

        // Then
        assertEquals(1, result);
        verify(tagMapper, times(1)).insert(any(Tag.class));
    }

    @Test
    @DisplayName("根据ID查询")
    void testSelectById() {
        // Given
        when(tagMapper.selectById(1L)).thenReturn(testTag);

        // When
        Tag result = tagMapper.selectById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testTag.getName(), result.getName());
        verify(tagMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("更新标签")
    void testUpdateById() {
        // Given
        when(tagMapper.updateById(any(Tag.class))).thenReturn(1);

        // When
        int result = tagMapper.updateById(testTag);

        // Then
        assertEquals(1, result);
        verify(tagMapper, times(1)).updateById(any(Tag.class));
    }

    @Test
    @DisplayName("删除标签")
    void testDeleteById() {
        // Given
        when(tagMapper.deleteById(1L)).thenReturn(1);

        // When
        int result = tagMapper.deleteById(1L);

        // Then
        assertEquals(1, result);
        verify(tagMapper, times(1)).deleteById(1L);
    }
}

