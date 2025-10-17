package com.xu.news.controller;

import com.alibaba.fastjson2.JSON;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

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
     * 获取知识库列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<KnowledgeEntry>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<KnowledgeEntry> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
            
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KnowledgeEntry> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like("title", keyword).or().like("content", keyword);
            }
            
            wrapper.eq("deleted", 0);
            wrapper.orderByDesc("created_at");
            
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<KnowledgeEntry> result = 
                knowledgeEntryService.page(pageObj, wrapper);
            
            return Result.success(PageResult.from(result));
        } catch (Exception e) {
            log.error("获取知识库列表失败: {}", e.getMessage(), e);
            return Result.error("获取列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建知识条目
     */
    @PostMapping
    public Result<KnowledgeEntry> create(@RequestBody KnowledgeEntry entry, Authentication authentication) {
        try {
            // 获取用户ID，如果未认证则使用默认值1L
            Long userId = 1L;
            if (authentication != null && authentication.getPrincipal() != null) {
                userId = (Long) authentication.getPrincipal();
            }
            
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
     * 上传文件并导入知识库（文本内容保存到数据库，不保留原始文件）
     */
    @PostMapping("/upload")
    public Result<KnowledgeEntry> upload(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication authentication) {
        try {
            // 获取用户ID
            Long userId = 1L;
            if (authentication != null && authentication.getPrincipal() != null) {
                userId = (Long) authentication.getPrincipal();
            }
            
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            log.info("📤 接收上传文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            
            // 直接从内存中提取文本并保存到数据库，不保存原始文件
            KnowledgeEntry entry = knowledgeEntryService.createFromUploadedFile(file, userId);
            
            log.info("✅ 文件上传成功: {}, 知识条目ID: {}", file.getOriginalFilename(), entry.getId());
            return Result.success("上传成功，文本内容已保存到知识库", entry);
        } catch (IOException e) {
            log.error("❌ 文件上传失败 - IO错误: {}", e.getMessage(), e);
            return Result.error("文件处理失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ 文件上传失败 - 未知错误: {}", e.getMessage(), e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * N8N 导入知识条目（无需认证）
     */
    @PostMapping("/import")
    public Result<KnowledgeEntry> importFromN8N(@RequestBody Map<String, Object> rawData) {
        try {
            log.info("N8N导入原始数据: {}", rawData);
            
            // 手动构建 KnowledgeEntry
            KnowledgeEntry entry = new KnowledgeEntry();
            entry.setTitle((String) rawData.get("title"));
            entry.setContent((String) rawData.get("content"));
            entry.setSummary((String) rawData.get("summary"));
            entry.setSourceUrl((String) rawData.get("sourceUrl"));
            entry.setAuthor((String) rawData.get("author"));
            entry.setContentType((String) rawData.get("contentType"));
            
            // 处理 tags（可能是数组或字符串）
            Object tagsObj = rawData.get("tags");
            if (tagsObj != null) {
                if (tagsObj instanceof String) {
                    entry.setTags((String) tagsObj);
                } else {
                    entry.setTags(JSON.toJSONString(tagsObj));
                }
            }
            
            // 处理时间字段
            Object publishedAtObj = rawData.get("publishedAt");
            if (publishedAtObj instanceof String) {
                try {
                    entry.setPublishedAt(LocalDateTime.parse((String) publishedAtObj));
                } catch (Exception e) {
                    log.warn("时间格式解析失败: {}", publishedAtObj);
                }
            }
            
            // 验证必填字段
            if (entry.getTitle() == null || entry.getTitle().isEmpty()) {
                return Result.error("标题不能为空");
            }
            if (entry.getContent() == null || entry.getContent().isEmpty()) {
                return Result.error("内容不能为空");
            }
            
            entry.setCreatedBy(1L);
            KnowledgeEntry created = knowledgeEntryService.createWithVector(entry);
            return Result.success("导入成功", created);
        } catch (Exception e) {
            log.error("N8N导入知识条目失败: {}", e.getMessage(), e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}

