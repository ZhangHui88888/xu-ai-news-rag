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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            log.info("📋 获取知识库列表 - 页码:{}, 每页:{}, 关键词:{}, 类型:{}", page, size, keyword, contentType);
            
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<KnowledgeEntry> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
            
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KnowledgeEntry> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
            }
            
            // 类型筛选
            if (contentType != null && !contentType.isEmpty()) {
                wrapper.eq("content_type", contentType);
            }
            
            // 时间范围筛选
            if (startDate != null && !startDate.isEmpty()) {
                wrapper.ge("created_at", startDate + " 00:00:00");
            }
            if (endDate != null && !endDate.isEmpty()) {
                wrapper.le("created_at", endDate + " 23:59:59");
            }
            
            wrapper.eq("deleted", 0);
            wrapper.orderByDesc("created_at");
            
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<KnowledgeEntry> result = 
                knowledgeEntryService.page(pageObj, wrapper);
            
            PageResult<KnowledgeEntry> pageResult = PageResult.from(result);
            
            log.info("✅ 查询成功 - 总数:{}, 当前页:{}, 返回记录数:{}", 
                pageResult.getTotal(), pageResult.getCurrent(), pageResult.getRecords().size());
            
            return Result.success(pageResult);
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
     * 批量删除知识条目
     */
    @PostMapping("/batch-delete")
    public Result<Map<String, Object>> batchDelete(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Long> ids = (java.util.List<Long>) params.get("ids");
            
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的条目");
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (Long id : ids) {
                try {
                    boolean success = knowledgeEntryService.deleteWithVector(id);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("删除知识条目{}失败: {}", id, e.getMessage());
                    failCount++;
                }
            }
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("total", ids.size());
            
            if (failCount == 0) {
                return Result.success("批量删除成功", result);
            } else if (successCount == 0) {
                return Result.error("批量删除失败");
            } else {
                return Result.success("部分删除成功", result);
            }
        } catch (Exception e) {
            log.error("批量删除失败: {}", e.getMessage(), e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新知识条目元数据
     */
    @PutMapping("/{id}")
    public Result<KnowledgeEntry> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            KnowledgeEntry entry = knowledgeEntryService.getById(id);
            if (entry == null) {
                return Result.notFound("知识条目不存在");
            }
            
            // 更新允许修改的字段
            if (updates.containsKey("title")) {
                entry.setTitle((String) updates.get("title"));
            }
            if (updates.containsKey("summary")) {
                entry.setSummary((String) updates.get("summary"));
            }
            if (updates.containsKey("sourceName")) {
                entry.setSourceName((String) updates.get("sourceName"));
            }
            if (updates.containsKey("sourceUrl")) {
                entry.setSourceUrl((String) updates.get("sourceUrl"));
            }
            if (updates.containsKey("author")) {
                entry.setAuthor((String) updates.get("author"));
            }
            if (updates.containsKey("tags")) {
                Object tagsObj = updates.get("tags");
                if (tagsObj instanceof String) {
                    entry.setTags((String) tagsObj);
                } else {
                    entry.setTags(JSON.toJSONString(tagsObj));
                }
            }
            if (updates.containsKey("contentType")) {
                entry.setContentType((String) updates.get("contentType"));
            }
            
            boolean success = knowledgeEntryService.updateById(entry);
            if (success) {
                return Result.success("更新成功", entry);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新知识条目失败: {}", e.getMessage(), e);
            return Result.error("更新失败: " + e.getMessage());
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
            
            // 设置来源名称，如果为空则使用默认值
            String sourceName = (String) rawData.get("sourceName");
            if (sourceName == null || sourceName.isEmpty()) {
                sourceName = "n8n自动读取";
            }
            entry.setSourceName(sourceName);
            
            // 处理 tags（可能是数组或字符串）
            // 只有当tags有实际内容时才设置，否则留空让AI自动生成
            Object tagsObj = rawData.get("tags");
            if (tagsObj != null) {
                String tagsStr = null;
                if (tagsObj instanceof String) {
                    tagsStr = (String) tagsObj;
                } else {
                    tagsStr = JSON.toJSONString(tagsObj);
                }
                
                // 只有当tags不为空且不是空数组时才设置
                if (tagsStr != null && !tagsStr.trim().isEmpty() && 
                    !tagsStr.equals("[]") && !tagsStr.equals("{}")) {
                    entry.setTags(tagsStr);
                    log.info("使用n8n提供的标签: {}", tagsStr);
                } else {
                    log.info("n8n未提供有效标签，将由AI自动生成");
                }
            } else {
                log.info("n8n未提供标签，将由AI自动生成");
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

