package com.xu.news.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xu.news.dto.SearchRequest;
import com.xu.news.entity.KnowledgeEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 知识条目服务接口
 * 
 * @author XU
 * @since 2025-10-15
 */
public interface KnowledgeEntryService extends IService<KnowledgeEntry> {

    /**
     * 创建知识条目并生成向量
     */
    KnowledgeEntry createWithVector(KnowledgeEntry entry) throws IOException;

    /**
     * 从文件创建知识条目
     */
    KnowledgeEntry createFromFile(File file, Long createdBy) throws IOException;

    /**
     * 从上传的文件创建知识条目（直接从内存，不保存文件）
     */
    KnowledgeEntry createFromUploadedFile(org.springframework.web.multipart.MultipartFile file, Long createdBy) throws IOException;

    /**
     * 搜索知识条目
     */
    Page<KnowledgeEntry> search(SearchRequest request);

    /**
     * 根据向量ID列表查询知识条目
     */
    List<KnowledgeEntry> findByVectorIds(List<Long> vectorIds);

    /**
     * 删除知识条目（同时删除向量）
     */
    boolean deleteWithVector(Long id);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);

    /**
     * 生成摘要
     */
    String generateSummary(String content) throws IOException;
}

