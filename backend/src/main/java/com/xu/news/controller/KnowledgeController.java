package com.xu.news.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xu.news.common.PageResult;
import com.xu.news.common.Result;
import com.xu.news.dto.SearchRequest;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.service.KnowledgeEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库控制器
 * 
 * @author XU
 * @since 2025-10-15
 */
@Slf4j
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeEntryService knowledgeEntryService;

    /**
     * 创建知识条目
     */
    @PostMapping
    public Result<KnowledgeEntry> create(@RequestBody KnowledgeEntry entry, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            entry.setCreatedBy(userId);
            KnowledgeEntry created = knowledgeEntryService.createWithVector(entry);
            return Result.success("创建成功", created);
        } catch (Exception e) {
            log.error("创建知识条目失败: {}", e.getMessage(), e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 搜索知识条目
     */
    @PostMapping("/search")
    public Result<PageResult<KnowledgeEntry>> search(@RequestBody SearchRequest request) {
        try {
            Page<KnowledgeEntry> page = knowledgeEntryService.search(request);
            return Result.success(PageResult.from(page));
        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取知识条目详情
     */
    @GetMapping("/{id}")
    public Result<KnowledgeEntry> getById(@PathVariable Long id) {
        try {
            KnowledgeEntry entry = knowledgeEntryService.getById(id);
            if (entry == null) {
                return Result.notFound("知识条目不存在");
            }
            // 增加浏览次数
            knowledgeEntryService.incrementViewCount(id);
            return Result.success(entry);
        } catch (Exception e) {
            log.error("获取知识条目失败: {}", e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 删除知识条目
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            boolean success = knowledgeEntryService.deleteWithVector(id);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除知识条目失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * N8N 导入知识条目（无需认证）
     */
    @PostMapping("/import")
    public Result<KnowledgeEntry> importFromN8N(@RequestBody KnowledgeEntry entry) {
        try {
            log.info("N8N导入知识条目: title={}, sourceUrl={}", entry.getTitle(), entry.getSourceUrl());
            // N8N 导入不需要用户ID
            entry.setCreatedBy(1L); // 使用系统用户ID
            KnowledgeEntry created = knowledgeEntryService.createWithVector(entry);
            return Result.success("导入成功", created);
        } catch (Exception e) {
            log.error("N8N导入知识条目失败: {}", e.getMessage(), e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}

