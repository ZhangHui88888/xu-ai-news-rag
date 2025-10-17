package com.xu.news.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xu.news.common.Result;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.entity.User;
import com.xu.news.service.KnowledgeEntryService;
import com.xu.news.service.UserService;
import com.xu.news.mapper.UserQueryHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统统计控制器
 * 
 * @author XU
 * @since 2025-10-18
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private KnowledgeEntryService knowledgeEntryService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserQueryHistoryMapper queryHistoryMapper;

    /**
     * 获取系统统计信息
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 统计知识条目数量（未删除的）
            QueryWrapper<KnowledgeEntry> knowledgeWrapper = new QueryWrapper<>();
            knowledgeWrapper.eq("deleted", 0);
            long knowledgeCount = knowledgeEntryService.count(knowledgeWrapper);
            
            // 统计查询次数
            long queryCount = queryHistoryMapper.selectCount(null);
            
            // 统计用户数量（未删除的）
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("deleted", 0);
            long userCount = userService.count(userWrapper);
            
            stats.put("knowledgeCount", knowledgeCount);
            stats.put("queryCount", queryCount);
            stats.put("userCount", userCount);
            
            log.debug("统计数据: 知识条目={}, 查询次数={}, 用户数={}", 
                knowledgeCount, queryCount, userCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取详细统计信息
     */
    @GetMapping("/detailed")
    public Result<Map<String, Object>> getDetailedStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计
            QueryWrapper<KnowledgeEntry> knowledgeWrapper = new QueryWrapper<>();
            knowledgeWrapper.eq("deleted", 0);
            long knowledgeCount = knowledgeEntryService.count(knowledgeWrapper);
            
            long queryCount = queryHistoryMapper.selectCount(null);
            
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("deleted", 0);
            long userCount = userService.count(userWrapper);
            
            // 按类型统计知识条目
            Map<String, Long> knowledgeByType = new HashMap<>();
            knowledgeByType.put("news", knowledgeEntryService.count(
                new QueryWrapper<KnowledgeEntry>().eq("content_type", "news").eq("deleted", 0)));
            knowledgeByType.put("article", knowledgeEntryService.count(
                new QueryWrapper<KnowledgeEntry>().eq("content_type", "article").eq("deleted", 0)));
            knowledgeByType.put("document", knowledgeEntryService.count(
                new QueryWrapper<KnowledgeEntry>().eq("content_type", "document").eq("deleted", 0)));
            knowledgeByType.put("test", knowledgeEntryService.count(
                new QueryWrapper<KnowledgeEntry>().eq("content_type", "test").eq("deleted", 0)));
            knowledgeByType.put("other", knowledgeEntryService.count(
                new QueryWrapper<KnowledgeEntry>().eq("content_type", "other").eq("deleted", 0)));
            
            stats.put("knowledgeCount", knowledgeCount);
            stats.put("queryCount", queryCount);
            stats.put("userCount", userCount);
            stats.put("knowledgeByType", knowledgeByType);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取详细统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取详细统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取关键词Top10分析
     */
    @GetMapping("/keywords-top10")
    public Result<java.util.List<Map<String, Object>>> getKeywordsTop10() {
        try {
            // 获取所有未删除的知识条目的tags
            QueryWrapper<KnowledgeEntry> wrapper = new QueryWrapper<>();
            wrapper.eq("deleted", 0);
            wrapper.isNotNull("tags");
            wrapper.ne("tags", "");
            wrapper.select("tags");
            
            java.util.List<KnowledgeEntry> entries = knowledgeEntryService.list(wrapper);
            
            // 统计所有标签的出现次数
            Map<String, Integer> tagCountMap = new HashMap<>();
            
            for (KnowledgeEntry entry : entries) {
                String tagsJson = entry.getTags();
                if (tagsJson != null && !tagsJson.trim().isEmpty()) {
                    try {
                        // 解析JSON数组
                        com.alibaba.fastjson2.JSONArray tagsArray = com.alibaba.fastjson2.JSON.parseArray(tagsJson);
                        for (int i = 0; i < tagsArray.size(); i++) {
                            String tag = tagsArray.getString(i);
                            if (tag != null && !tag.trim().isEmpty()) {
                                tag = tag.trim();
                                tagCountMap.put(tag, tagCountMap.getOrDefault(tag, 0) + 1);
                            }
                        }
                    } catch (Exception e) {
                        // 如果不是JSON格式，尝试按逗号分割
                        String[] tags = tagsJson.split(",");
                        for (String tag : tags) {
                            tag = tag.trim();
                            if (!tag.isEmpty()) {
                                tagCountMap.put(tag, tagCountMap.getOrDefault(tag, 0) + 1);
                            }
                        }
                    }
                }
            }
            
            // 按出现次数排序，取Top10
            java.util.List<Map<String, Object>> topKeywords = tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("keyword", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
            
            log.info("关键词Top10统计完成，总标签数: {}, Top10: {}", tagCountMap.size(), topKeywords.size());
            
            return Result.success(topKeywords);
        } catch (Exception e) {
            log.error("获取关键词Top10失败: {}", e.getMessage(), e);
            return Result.error("获取关键词Top10失败: " + e.getMessage());
        }
    }
}

