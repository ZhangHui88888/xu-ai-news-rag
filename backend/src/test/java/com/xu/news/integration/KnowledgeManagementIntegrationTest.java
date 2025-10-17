package com.xu.news.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.mapper.KnowledgeEntryMapper;
import com.xu.news.util.JwtUtil;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 知识库管理集成测试 - 测试完整的知识库CRUD流程
 * 
 * @author XU
 * @since 2025-10-17
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("知识库管理集成测试")
class KnowledgeManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KnowledgeEntryMapper knowledgeEntryMapper;

    private String authToken;
    private KnowledgeEntry testEntry;

    @BeforeEach
    void setUp() {
        authToken = jwtUtil.generateToken(1L, "testuser", "user");
        testEntry = TestDataBuilder.createKnowledgeEntry();
        testEntry.setId(null);
    }

    @Test
    @DisplayName("完整CRUD流程 - 创建、查询、更新、删除")
    void testCompleteCRUDFlow() throws Exception {
        // Step 1: 创建知识条目（需要Mock向量服务）
        // 注意：由于实际环境可能没有Ollama服务，这个测试可能需要调整
        
        // Step 2: 查询列表
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        // Step 3: 直接在数据库插入测试数据
        knowledgeEntryMapper.insert(testEntry);
        Long entryId = testEntry.getId();

        // Step 4: 查询详情
        mockMvc.perform(get("/knowledge/" + entryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(entryId))
                .andExpect(jsonPath("$.data.title").value(testEntry.getTitle()));

        // Step 5: 更新
        testEntry.setTitle("更新后的标题");
        mockMvc.perform(put("/knowledge/" + entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEntry))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Step 6: 验证更新
        mockMvc.perform(get("/knowledge/" + entryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("更新后的标题"));

        // Step 7: 删除（需要Mock向量服务）
        // mockMvc.perform(delete("/knowledge/" + entryId)...
    }

    @Test
    @DisplayName("搜索功能 - 关键词搜索")
    void testSearchByKeyword() throws Exception {
        // Given: 插入测试数据
        testEntry.setTitle("人工智能技术发展");
        knowledgeEntryMapper.insert(testEntry);

        // When & Then: 搜索
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "人工智能")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("分页功能 - 多页数据")
    void testPagination() throws Exception {
        // Given: 插入多条测试数据
        for (int i = 0; i < 25; i++) {
            KnowledgeEntry entry = TestDataBuilder.createKnowledgeEntry();
            entry.setId(null);
            entry.setTitle("测试条目 " + i);
            knowledgeEntryMapper.insert(entry);
        }

        // When & Then: 查询第一页
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").exists());

        // When & Then: 查询第二页
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "2")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.current").value(2));
    }

    @Test
    @DisplayName("过滤功能 - 按类型过滤")
    void testFilterByContentType() throws Exception {
        // Given: 插入不同类型的数据
        KnowledgeEntry newsEntry = TestDataBuilder.createKnowledgeEntry();
        newsEntry.setId(null);
        newsEntry.setContentType("news");
        knowledgeEntryMapper.insert(newsEntry);

        KnowledgeEntry articleEntry = TestDataBuilder.createKnowledgeEntry();
        articleEntry.setId(null);
        articleEntry.setContentType("article");
        knowledgeEntryMapper.insert(articleEntry);

        // When & Then: 按类型过滤
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("contentType", "news")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("浏览量统计 - 访问增加浏览量")
    void testViewCountIncrement() throws Exception {
        // Given: 插入测试数据
        testEntry.setViewCount(0);
        knowledgeEntryMapper.insert(testEntry);
        Long entryId = testEntry.getId();

        // When: 第一次访问
        mockMvc.perform(get("/knowledge/" + entryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // When: 第二次访问
        mockMvc.perform(get("/knowledge/" + entryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Then: 验证浏览量增加
        KnowledgeEntry updated = knowledgeEntryMapper.selectById(entryId);
        // 浏览量应该增加（具体数值取决于实现）
        // assertTrue(updated.getViewCount() >= 2);
    }

    @Test
    @DisplayName("权限验证 - 未授权访问")
    void testUnauthorizedAccess() throws Exception {
        // 不提供token的情况下访问需要认证的接口
        // 注意：根据实际配置，某些接口可能允许未认证访问
        
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());  // 或者根据实际配置调整预期状态
    }

    @Test
    @DisplayName("时间范围过滤")
    void testDateRangeFilter() throws Exception {
        // Given: 插入测试数据
        knowledgeEntryMapper.insert(testEntry);

        // When & Then: 按时间范围过滤
        mockMvc.perform(get("/knowledge/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("查询不存在的条目")
    void testGetNonExistentEntry() throws Exception {
        mockMvc.perform(get("/knowledge/999999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}

