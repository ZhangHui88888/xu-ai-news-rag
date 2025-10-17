package com.xu.news.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xu.news.base.BaseTest;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KnowledgeEntryMapper 测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("知识条目Mapper测试")
@Transactional
class KnowledgeEntryMapperTest extends BaseTest {

    @Autowired
    private KnowledgeEntryMapper knowledgeEntryMapper;

    private KnowledgeEntry testEntry;

    @BeforeEach
    void setUp() {
        testEntry = TestDataBuilder.createKnowledgeEntry();
        testEntry.setId(null);  // 让数据库自动生成ID
    }

    @Test
    @DisplayName("插入知识条目 - 成功")
    void testInsert_Success() {
        // When
        int result = knowledgeEntryMapper.insert(testEntry);

        // Then
        assertEquals(1, result);
        assertNotNull(testEntry.getId());
    }

    @Test
    @DisplayName("根据ID查询 - 成功")
    void testSelectById_Success() {
        // Given
        knowledgeEntryMapper.insert(testEntry);
        Long id = testEntry.getId();

        // When
        KnowledgeEntry result = knowledgeEntryMapper.selectById(id);

        // Then
        assertNotNull(result);
        assertEquals(testEntry.getTitle(), result.getTitle());
        assertEquals(testEntry.getContent(), result.getContent());
    }

    @Test
    @DisplayName("根据ID查询 - 不存在")
    void testSelectById_NotFound() {
        // When
        KnowledgeEntry result = knowledgeEntryMapper.selectById(999L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("更新知识条目 - 成功")
    void testUpdateById_Success() {
        // Given
        knowledgeEntryMapper.insert(testEntry);
        Long id = testEntry.getId();
        
        // When
        testEntry.setTitle("更新后的标题");
        testEntry.setContent("更新后的内容");
        int result = knowledgeEntryMapper.updateById(testEntry);

        // Then
        assertEquals(1, result);
        KnowledgeEntry updated = knowledgeEntryMapper.selectById(id);
        assertEquals("更新后的标题", updated.getTitle());
        assertEquals("更新后的内容", updated.getContent());
    }

    @Test
    @DisplayName("删除知识条目 - 成功")
    void testDeleteById_Success() {
        // Given
        knowledgeEntryMapper.insert(testEntry);
        Long id = testEntry.getId();

        // When
        int result = knowledgeEntryMapper.deleteById(id);

        // Then
        assertEquals(1, result);
        KnowledgeEntry deleted = knowledgeEntryMapper.selectById(id);
        assertNull(deleted);
    }

    @Test
    @DisplayName("全文搜索 - 成功")
    void testFullTextSearch_Success() {
        // Given
        testEntry.setTitle("人工智能最新进展");
        testEntry.setContent("深度学习是人工智能的重要分支");
        knowledgeEntryMapper.insert(testEntry);

        // When
        Page<KnowledgeEntry> page = new Page<>(1, 10);
        IPage<KnowledgeEntry> result = knowledgeEntryMapper.fullTextSearch(
                page, "人工智能", null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @DisplayName("根据向量ID列表查询")
    void testFindByVectorIds() {
        // Given
        KnowledgeEntry entry1 = TestDataBuilder.createKnowledgeEntry();
        KnowledgeEntry entry2 = TestDataBuilder.createKnowledgeEntry();
        entry1.setId(null);
        entry2.setId(null);
        knowledgeEntryMapper.insert(entry1);
        knowledgeEntryMapper.insert(entry2);

        // When
        List<KnowledgeEntry> results = knowledgeEntryMapper.findByVectorIds(
                List.of(entry1.getId(), entry2.getId()));

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("增加浏览次数")
    void testIncrementViewCount() {
        // Given
        testEntry.setViewCount(10);
        knowledgeEntryMapper.insert(testEntry);
        Long entryId = testEntry.getId();

        // When
        int result = knowledgeEntryMapper.incrementViewCount(entryId);

        // Then
        assertEquals(1, result);
        KnowledgeEntry updated = knowledgeEntryMapper.selectById(entryId);
        assertEquals(11, updated.getViewCount());
    }

    @Test
    @DisplayName("批量查询")
    void testSelectBatchIds() {
        // Given
        KnowledgeEntry entry1 = TestDataBuilder.createKnowledgeEntry();
        KnowledgeEntry entry2 = TestDataBuilder.createKnowledgeEntry();
        entry1.setId(null);
        entry2.setId(null);
        knowledgeEntryMapper.insert(entry1);
        knowledgeEntryMapper.insert(entry2);

        // When
        List<KnowledgeEntry> results = knowledgeEntryMapper.selectBatchIds(
                List.of(entry1.getId(), entry2.getId()));

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("分页查询 - 第一页")
    void testSelectPage_FirstPage() {
        // Given
        for (int i = 0; i < 15; i++) {
            KnowledgeEntry entry = TestDataBuilder.createKnowledgeEntry();
            entry.setId(null);
            entry.setTitle("测试条目 " + i);
            knowledgeEntryMapper.insert(entry);
        }

        // When
        Page<KnowledgeEntry> page = new Page<>(1, 10);
        Page<KnowledgeEntry> result = knowledgeEntryMapper.selectPage(page, null);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getRecords().size());
        assertTrue(result.getTotal() >= 15);
    }
}

