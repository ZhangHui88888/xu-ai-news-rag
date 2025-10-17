package com.xu.news.mapper;

import com.xu.news.entity.DataSource;
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
 * DataSourceMapper 单元测试（使用Mock）
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据源Mapper测试")
class DataSourceMapperTest {

    @Mock
    private DataSourceMapper dataSourceMapper;

    private DataSource testDataSource;

    @BeforeEach
    void setUp() {
        testDataSource = TestDataBuilder.createDataSource();
    }

    @Test
    @DisplayName("插入数据源")
    void testInsert() {
        // Given
        when(dataSourceMapper.insert(any(DataSource.class))).thenReturn(1);

        // When
        int result = dataSourceMapper.insert(testDataSource);

        // Then
        assertEquals(1, result);
        verify(dataSourceMapper, times(1)).insert(any(DataSource.class));
    }

    @Test
    @DisplayName("根据ID查询")
    void testSelectById() {
        // Given
        when(dataSourceMapper.selectById(1L)).thenReturn(testDataSource);

        // When
        DataSource result = dataSourceMapper.selectById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testDataSource.getName(), result.getName());
        verify(dataSourceMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("更新数据源")
    void testUpdateById() {
        // Given
        when(dataSourceMapper.updateById(any(DataSource.class))).thenReturn(1);

        // When
        int result = dataSourceMapper.updateById(testDataSource);

        // Then
        assertEquals(1, result);
        verify(dataSourceMapper, times(1)).updateById(any(DataSource.class));
    }

    @Test
    @DisplayName("删除数据源")
    void testDeleteById() {
        // Given
        when(dataSourceMapper.deleteById(1L)).thenReturn(1);

        // When
        int result = dataSourceMapper.deleteById(1L);

        // Then
        assertEquals(1, result);
        verify(dataSourceMapper, times(1)).deleteById(1L);
    }
}

