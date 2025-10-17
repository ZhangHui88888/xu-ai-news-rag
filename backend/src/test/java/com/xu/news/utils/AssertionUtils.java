package com.xu.news.utils;

import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.entity.User;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 断言工具类 - 提供自定义断言方法
 * 
 * @author XU
 * @since 2025-10-17
 */
public class AssertionUtils {

    /**
     * 断言用户对象不为空且字段有效
     */
    public static void assertUserValid(User user) {
        assertNotNull(user, "用户对象不应为空");
        assertNotNull(user.getId(), "用户ID不应为空");
        assertNotNull(user.getUsername(), "用户名不应为空");
        assertNotNull(user.getEmail(), "邮箱不应为空");
        assertTrue(user.getUsername().length() > 0, "用户名长度应大于0");
    }

    /**
     * 断言知识条目对象不为空且字段有效
     */
    public static void assertKnowledgeEntryValid(KnowledgeEntry entry) {
        assertNotNull(entry, "知识条目对象不应为空");
        assertNotNull(entry.getId(), "知识条目ID不应为空");
        assertNotNull(entry.getTitle(), "标题不应为空");
        assertNotNull(entry.getContent(), "内容不应为空");
        assertTrue(entry.getTitle().length() > 0, "标题长度应大于0");
        assertTrue(entry.getContent().length() > 0, "内容长度应大于0");
    }

    /**
     * 断言两个用户对象相等（仅比较关键字段）
     */
    public static void assertUserEquals(User expected, User actual) {
        assertNotNull(actual, "实际用户对象不应为空");
        assertEquals(expected.getUsername(), actual.getUsername(), "用户名应相等");
        assertEquals(expected.getEmail(), actual.getEmail(), "邮箱应相等");
        assertEquals(expected.getRole(), actual.getRole(), "角色应相等");
    }

    /**
     * 断言分页结果有效
     */
    public static void assertPageValid(long total, long current, long size) {
        assertTrue(total >= 0, "总记录数应大于等于0");
        assertTrue(current > 0, "当前页码应大于0");
        assertTrue(size > 0, "每页大小应大于0");
    }
}

