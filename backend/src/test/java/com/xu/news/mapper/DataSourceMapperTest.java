package com.xu.news.mapper;

import com.xu.news.base.BaseTest;
import com.xu.news.entity.DataSource;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSourceMapper 测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("数据源Mapper测试")
@Transactional
class DataSourceMapperTest extends BaseTest {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    private DataSource testDataSource;

    @BeforeEach
    void setUp() {
        testDataSource = TestDataBuilder.createDataSource();
        testDataSource.setId(null);
    }

    @Test
    @DisplayName("插入数据源 - 成功")
    void testInsert_Success() {
        // When
        int result = dataSourceMapper.insert(testDataSource);

        // Then
        assertEquals(1, result);
        assertNotNull(testDataSource.getId());
    }

    @Test
    @DisplayName("根据ID查询 - 成功")
    void testSelectById_Success() {
        // Given
        dataSourceMapper.insert(testDataSource);
        Long id = testDataSource.getId();

        // When
        DataSource result = dataSourceMapper.selectById(id);

        // Then
        assertNotNull(result);
        assertEquals(testDataSource.getName(), result.getName());
        assertEquals(testDataSource.getSourceUrl(), result.getSourceUrl());
    }

    @Test
    @DisplayName("更新数据源 - 成功")
    void testUpdateById_Success() {
        // Given
        dataSourceMapper.insert(testDataSource);
        
        // When
        testDataSource.setName("更新后的名称");
        testDataSource.setSourceUrl("https://new-url.com");
        int result = dataSourceMapper.updateById(testDataSource);

        // Then
        assertEquals(1, result);
        DataSource updated = dataSourceMapper.selectById(testDataSource.getId());
        assertEquals("更新后的名称", updated.getName());
        assertEquals("https://new-url.com", updated.getSourceUrl());
    }

    @Test
    @DisplayName("删除数据源 - 成功")
    void testDeleteById_Success() {
        // Given
        dataSourceMapper.insert(testDataSource);
        Long id = testDataSource.getId();

        // When
        int result = dataSourceMapper.deleteById(id);

        // Then
        assertEquals(1, result);
    }

    @Test
    @DisplayName("查询所有启用的数据源")
    void testSelectEnabledSources() {
        // Given
        testDataSource.setEnabled(1);
        dataSourceMapper.insert(testDataSource);
        
        DataSource disabledSource = TestDataBuilder.createDataSource();
        disabledSource.setId(null);
        disabledSource.setEnabled(0);
        dataSourceMapper.insert(disabledSource);

        // When
        List<DataSource> results = dataSourceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSource>()
                        .eq("enabled", 1)
        );

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.stream().allMatch(ds -> ds.getEnabled() == 1));
    }

    @Test
    @DisplayName("根据类型查询数据源")
    void testSelectBySourceType() {
        // Given
        testDataSource.setSourceType("RSS");
        dataSourceMapper.insert(testDataSource);

        // When
        List<DataSource> results = dataSourceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSource>()
                        .eq("source_type", "RSS")
        );

        // Then
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals("RSS", results.get(0).getSourceType());
    }

    @Test
    @DisplayName("根据名称查询数据源")
    void testSelectByName() {
        // Given
        String uniqueName = "UniqueSourceName";
        testDataSource.setName(uniqueName);
        dataSourceMapper.insert(testDataSource);

        // When
        List<DataSource> results = dataSourceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSource>()
                        .eq("name", uniqueName)
        );

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(uniqueName, results.get(0).getName());
    }
}

