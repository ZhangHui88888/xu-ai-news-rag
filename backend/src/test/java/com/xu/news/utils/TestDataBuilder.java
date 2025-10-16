package com.xu.news.utils;

import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.dto.QueryRequest;
import com.xu.news.entity.User;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.entity.DataSource;
import com.xu.news.entity.Tag;

import java.time.LocalDateTime;

/**
 * 测试数据构建器
 * 
 * @author XU
 * @since 2025-10-16
 */
public class TestDataBuilder {

    /**
     * 创建测试用户
     */
    public static User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH");
        user.setEmail("test@example.com");
        user.setFullName("测试用户");
        user.setRole("user");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);
        return user;
    }

    /**
     * 创建测试管理员
     */
    public static User createTestAdmin() {
        User user = new User();
        user.setId(2L);
        user.setUsername("admin");
        user.setPasswordHash("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH");
        user.setEmail("admin@example.com");
        user.setFullName("管理员");
        user.setRole("admin");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);
        return user;
    }

    /**
     * 创建注册请求
     */
    public static RegisterRequest createRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("Test123456");
        request.setEmail("newuser@example.com");
        request.setFullName("新用户");
        return request;
    }

    /**
     * 创建登录请求
     */
    public static LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Test123456");
        return request;
    }

    /**
     * 创建查询请求
     */
    public static QueryRequest createQueryRequest() {
        QueryRequest request = new QueryRequest();
        request.setQuery("什么是人工智能？");
        request.setTopK(5);
        request.setNeedAnswer(true);
        return request;
    }

    /**
     * 创建知识库条目
     */
    public static KnowledgeEntry createKnowledgeEntry() {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setId(1L);
        entry.setTitle("AI技术最新进展");
        entry.setContent("人工智能技术在2025年取得了重大突破，特别是在大语言模型、计算机视觉和机器学习等领域。");
        entry.setSummary("AI在多个领域取得进展");
        entry.setContentType("news");
        entry.setSourceUrl("https://example.com/ai-news-1");
        entry.setSourceName("TechCrunch");
        entry.setAuthor("John Doe");
        entry.setPublishedAt(LocalDateTime.now());
        // Note: userId和dataSourceId可能需要根据实际字段名调整
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setDeleted(0);
        return entry;
    }

    /**
     * 创建数据源
     */
    public static DataSource createDataSource() {
        DataSource source = new DataSource();
        source.setId(1L);
        source.setName("TechCrunch");
        source.setSourceType("RSS");
        source.setSourceUrl("https://techcrunch.com/feed/");
        source.setStatus(1);
        source.setCreatedAt(LocalDateTime.now());
        source.setUpdatedAt(LocalDateTime.now());
        source.setDeleted(0);
        return source;
    }

    /**
     * 创建标签
     */
    public static Tag createTag() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("AI");
        tag.setColor("#3b82f6");
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        tag.setDeleted(0);
        return tag;
    }

    /**
     * 生成测试JWT Token
     */
    public static String generateTestToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoidGVzdHVzZXIiLCJyb2xlIjoiVVNFUiJ9.test-signature";
    }
}

