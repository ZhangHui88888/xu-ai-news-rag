package com.xu.news.service;

import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;

import java.io.IOException;

/**
 * 智能问答服务接口（RAG核心）
 * 
 * @author XU
 * @since 2025-10-15
 */
public interface QueryService {

    /**
     * 处理用户查询（RAG流程）
     * 1. 将问题向量化
     * 2. 在FAISS中检索相关文档
     * 3. 使用LLM生成回答
     * 4. 保存查询历史
     */
    QueryResponse query(QueryRequest request, Long userId) throws IOException;

    /**
     * 语义搜索（只返回相关文档，不生成回答）
     */
    QueryResponse semanticSearch(QueryRequest request, Long userId) throws IOException;
}

