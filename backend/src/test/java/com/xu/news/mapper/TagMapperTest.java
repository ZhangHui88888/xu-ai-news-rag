package com.xu.news.mapper;

import com.xu.news.base.BaseTest;
import com.xu.news.entity.Tag;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TagMapper 测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("标签Mapper测试")
@Transactional
class TagMapperTest extends BaseTest {

    @Autowired
    private TagMapper tagMapper;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = TestDataBuilder.createTag();
        testTag.setId(null);
    }

    @Test
    @DisplayName("插入标签 - 成功")
    void testInsert_Success() {
        // When
        int result = tagMapper.insert(testTag);

        // Then
        assertEquals(1, result);
        assertNotNull(testTag.getId());
    }

    @Test
    @DisplayName("根据ID查询 - 成功")
    void testSelectById_Success() {
        // Given
        tagMapper.insert(testTag);
        Long id = testTag.getId();

        // When
        Tag result = tagMapper.selectById(id);

        // Then
        assertNotNull(result);
        assertEquals(testTag.getName(), result.getName());
        assertEquals(testTag.getColor(), result.getColor());
    }

    @Test
    @DisplayName("更新标签 - 成功")
    void testUpdateById_Success() {
        // Given
        tagMapper.insert(testTag);
        
        // When
        testTag.setName("新标签名");
        testTag.setColor("#ff0000");
        int result = tagMapper.updateById(testTag);

        // Then
        assertEquals(1, result);
        Tag updated = tagMapper.selectById(testTag.getId());
        assertEquals("新标签名", updated.getName());
        assertEquals("#ff0000", updated.getColor());
    }

    @Test
    @DisplayName("删除标签 - 成功")
    void testDeleteById_Success() {
        // Given
        tagMapper.insert(testTag);
        Long id = testTag.getId();

        // When
        int result = tagMapper.deleteById(id);

        // Then
        assertEquals(1, result);
    }

    @Test
    @DisplayName("根据名称查询标签")
    void testSelectByName() {
        // Given
        String uniqueName = "UniqueTagName";
        testTag.setName(uniqueName);
        tagMapper.insert(testTag);

        // When
        List<Tag> results = tagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Tag>()
                        .eq("name", uniqueName)
        );

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(uniqueName, results.get(0).getName());
    }

    @Test
    @DisplayName("查询所有标签")
    void testSelectAll() {
        // Given
        tagMapper.insert(testTag);
        Tag tag2 = TestDataBuilder.createTag();
        tag2.setId(null);
        tag2.setName("Machine Learning");
        tagMapper.insert(tag2);

        // When
        List<Tag> results = tagMapper.selectList(null);

        // Then
        assertNotNull(results);
        assertTrue(results.size() >= 2);
    }
}

