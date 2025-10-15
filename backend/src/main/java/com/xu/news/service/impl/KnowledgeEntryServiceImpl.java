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
        // 生成摘要（如果没有提供）
        if (entry.getSummary() == null || entry.getSummary().isEmpty()) {
            String summary = ollamaClient.generateSummary(entry.getContent());
            entry.setSummary(summary);
        }

        // 生成向量
        String textForEmbedding = entry.getTitle() + "\n" + entry.getContent();
        List<Double> vector = ollamaClient.generateEmbedding(textForEmbedding);
        
        // 存储向量
        Long vectorId = vectorStore.addVector(vector);
        entry.setVectorId(vectorId);
        entry.setVectorEmbedding(JSON.toJSONString(vector));

        // 保存到数据库
        knowledgeEntryMapper.insert(entry);
        
        log.info("创建知识条目成功: ID={}, VectorID={}", entry.getId(), vectorId);
        return entry;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeEntry createFromFile(File file, Long createdBy) throws IOException {
        // 提取文本内容
        String content = fileProcessor.extractText(file);
        content = fileProcessor.cleanText(content);

        // 创建知识条目
        KnowledgeEntry entry = new KnowledgeEntry()
                .setTitle(file.getName())
                .setContent(content)
                .setContentType("document")
                .setFilePath(file.getAbsolutePath())
                .setCreatedBy(createdBy)
                .setStatus(1);

        return createWithVector(entry);
    }

    @Override
    public Page<KnowledgeEntry> search(SearchRequest request) {
        Page<KnowledgeEntry> page = new Page<>(request.getCurrent(), request.getSize());
        
        return knowledgeEntryMapper.fullTextSearch(
            page,
            request.getKeyword(),
            request.getSourceIds(),
            request.getTags(),
            request.getStartDate(),
            request.getEndDate()
        );
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

