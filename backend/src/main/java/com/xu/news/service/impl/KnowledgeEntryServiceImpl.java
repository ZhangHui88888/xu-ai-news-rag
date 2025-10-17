package com.xu.news.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xu.news.dto.SearchRequest;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.mapper.KnowledgeEntryMapper;
import com.xu.news.service.KnowledgeEntryService;
import com.xu.news.util.FileProcessor;
import com.xu.news.util.OllamaClient;
import com.xu.news.util.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 知识条目服务实现类
 * 
 * @author XU
 * @since 2025-10-15
 */
@Slf4j
@Service
public class KnowledgeEntryServiceImpl extends ServiceImpl<KnowledgeEntryMapper, KnowledgeEntry> 
        implements KnowledgeEntryService {

    @Autowired
    private KnowledgeEntryMapper knowledgeEntryMapper;

    @Autowired
    private OllamaClient ollamaClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private FileProcessor fileProcessor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeEntry createWithVector(KnowledgeEntry entry) throws IOException {
        try {
            // 生成摘要（如果没有提供）
            if (entry.getSummary() == null || entry.getSummary().isEmpty()) {
                log.info("🤖 生成AI摘要...");
                String summary = ollamaClient.generateSummary(entry.getContent());
                entry.setSummary(summary);
                log.debug("摘要生成成功: {} 字符", summary.length());
            }

            // 生成标签（如果没有提供）
            if (entry.getTags() == null || entry.getTags().isEmpty()) {
                log.info("🏷️  生成AI标签...");
                String tags = ollamaClient.generateTags(entry.getTitle(), entry.getContent());
                entry.setTags(tags);
                log.debug("标签生成成功: {}", tags);
            }

            // 生成向量
            log.info("🧠 生成向量嵌入...");
            String textForEmbedding = entry.getTitle() + "\n" + entry.getContent();
            List<Double> vector = ollamaClient.generateEmbedding(textForEmbedding);
            log.debug("向量生成成功: 维度={}", vector.size());
            
            // 存储向量
            log.debug("存储向量到向量库...");
            Long vectorId = vectorStore.addVector(vector);
            entry.setVectorId(vectorId);
            entry.setVectorEmbedding(JSON.toJSONString(vector));
            log.debug("向量存储成功: VectorID={}", vectorId);

            // 保存到数据库
            log.info("💾 保存知识条目到数据库...");
            knowledgeEntryMapper.insert(entry);
            
            log.info("✅ 创建知识条目成功: ID={}, VectorID={}, 标题={}, 标签={}", 
                entry.getId(), vectorId, entry.getTitle(), entry.getTags());
            return entry;
            
        } catch (IOException e) {
            log.error("❌ 创建知识条目失败 - IO错误: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("❌ 创建知识条目失败 - 未知错误: {}", e.getMessage(), e);
            throw new IOException("创建知识条目失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeEntry createFromFile(File file, Long createdBy) throws IOException {
        log.info("开始处理文件: {}", file.getName());
        
        // 提取文本内容
        log.debug("提取文件文本内容...");
        String content = fileProcessor.extractText(file);
        log.debug("原始文本长度: {} 字符", content.length());
        
        content = fileProcessor.cleanText(content);
        log.debug("清理后文本长度: {} 字符", content.length());
        
        if (content == null || content.trim().isEmpty()) {
            throw new IOException("文件内容为空，无法提取文本");
        }

        // 创建知识条目
        KnowledgeEntry entry = new KnowledgeEntry()
                .setTitle(file.getName())
                .setContent(content)
                .setContentType("document")
                .setFilePath(file.getAbsolutePath())
                .setCreatedBy(createdBy)
                .setStatus(1);

        log.info("开始生成向量和保存到数据库...");
        return createWithVector(entry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeEntry createFromUploadedFile(org.springframework.web.multipart.MultipartFile file, Long createdBy) throws IOException {
        String originalFilename = file.getOriginalFilename();
        log.info("📤 开始处理上传文件: {}, 大小: {} bytes", originalFilename, file.getSize());
        
        // 直接从内存流中提取文本内容
        log.debug("从内存流提取文件文本内容...");
        String content = fileProcessor.extractText(file.getInputStream(), originalFilename);
        log.debug("原始文本长度: {} 字符", content.length());
        
        content = fileProcessor.cleanText(content);
        log.debug("清理后文本长度: {} 字符", content.length());
        
        if (content == null || content.trim().isEmpty()) {
            throw new IOException("文件内容为空，无法提取文本");
        }

        // 创建知识条目（不保存文件路径）
        KnowledgeEntry entry = new KnowledgeEntry()
                .setTitle(originalFilename)
                .setContent(content)
                .setContentType("document")
                .setFilePath(null)  // 不保存文件，所以路径为空
                .setCreatedBy(createdBy)
                .setStatus(1)
                .setLanguage("zh-CN")  // 设置语言
                .setSourceName("用户上传");  // 标记来源

        log.info("开始生成向量和保存到数据库...");
        return createWithVector(entry);
    }

    @Override
    public Page<KnowledgeEntry> search(SearchRequest request) {
        Page<KnowledgeEntry> page = new Page<>(request.getCurrent(), request.getSize());
        
        com.baomidou.mybatisplus.core.metadata.IPage<KnowledgeEntry> result = knowledgeEntryMapper.fullTextSearch(
            page,
            request.getKeyword(),
            request.getSourceIds(),
            request.getTags(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        // 将 IPage 转换为 Page
        return (Page<KnowledgeEntry>) result;
    }

    @Override
    public List<KnowledgeEntry> findByVectorIds(List<Long> vectorIds) {
        return knowledgeEntryMapper.findByVectorIds(vectorIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithVector(Long id) {
        KnowledgeEntry entry = knowledgeEntryMapper.selectById(id);
        if (entry == null) {
            return false;
        }

        // 删除向量
        if (entry.getVectorId() != null) {
            vectorStore.deleteVector(entry.getVectorId());
        }

        // 删除条目（逻辑删除）
        return knowledgeEntryMapper.deleteById(id) > 0;
    }

    @Override
    public void incrementViewCount(Long id) {
        knowledgeEntryMapper.incrementViewCount(id);
    }

    @Override
    public String generateSummary(String content) throws IOException {
        return ollamaClient.generateSummary(content);
    }
}

