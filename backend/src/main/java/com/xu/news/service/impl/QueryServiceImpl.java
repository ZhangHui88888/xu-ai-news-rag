package com.xu.news.service.impl;

import com.alibaba.fastjson2.JSON;
import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.entity.UserQueryHistory;
import com.xu.news.mapper.UserQueryHistoryMapper;
import com.xu.news.service.KnowledgeEntryService;
import com.xu.news.service.QueryService;
import com.xu.news.util.OllamaClient;
import com.xu.news.util.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能问答服务实现类
 * 
 * @author XU
 * @since 2025-10-15
 */
@Slf4j
@Service
public class QueryServiceImpl implements QueryService {

    @Autowired
    private OllamaClient ollamaClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private KnowledgeEntryService knowledgeEntryService;

    @Autowired
    private UserQueryHistoryMapper queryHistoryMapper;

    @Override
    public QueryResponse query(QueryRequest request, Long userId) throws IOException {
        long startTime = System.currentTimeMillis();

        // Step 1: 将问题向量化
        log.debug("生成查询向量: {}", request.getQuery());
        List<Double> queryVector = ollamaClient.generateEmbedding(request.getQuery());

        // Step 2: 在向量库中检索相关文档
        log.debug("检索相关文档，TopK={}, Threshold={}", request.getTopK(), request.getSimilarityThreshold());
        List<VectorStore.SearchResult> searchResults = vectorStore.searchWithThreshold(
            queryVector,
            request.getTopK(),
            request.getSimilarityThreshold()
        );

        // Step 3: 获取知识条目详情
        List<Long> vectorIds = searchResults.stream()
                .map(VectorStore.SearchResult::getVectorId)
                .collect(Collectors.toList());
        
        List<KnowledgeEntry> entries = new ArrayList<>();
        if (!vectorIds.isEmpty()) {
            entries = knowledgeEntryService.findByVectorIds(vectorIds);
        }

        // Step 4: 构建响应的检索结果
        List<QueryResponse.RetrievedEntry> retrievedEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            KnowledgeEntry entry = entries.get(i);
            Double score = i < searchResults.size() ? searchResults.get(i).getScore() : 0.0;
            
            QueryResponse.RetrievedEntry retrievedEntry = new QueryResponse.RetrievedEntry();
            retrievedEntry.setId(entry.getId());
            retrievedEntry.setTitle(entry.getTitle());
            retrievedEntry.setSummary(entry.getSummary());
            retrievedEntry.setSourceName(entry.getSourceName());
            retrievedEntry.setSourceUrl(entry.getSourceUrl());
            retrievedEntry.setSimilarityScore(score);
            retrievedEntry.setPublishedAt(entry.getPublishedAt() != null ? 
                    entry.getPublishedAt().toString() : null);
            retrievedEntry.setTags(entry.getTags() != null ? 
                    JSON.parseArray(entry.getTags(), String.class) : null);
            
            retrievedEntries.add(retrievedEntry);
        }

        // Step 5: 使用LLM生成回答（如果需要）
        String answer = null;
        if (request.getNeedAnswer()) {
            if (entries.isEmpty()) {
                // 知识库中没有相关内容，直接用LLM回答
                log.debug("知识库中未找到相关内容，使用LLM直接回答");
                answer = "抱歉，知识库中暂时没有找到与您问题相关的内容。这可能是因为：\n" +
                         "1. 知识库还没有导入相关数据\n" +
                         "2. 您的问题超出了当前知识库的范围\n\n" +
                         "建议：请先导入相关的新闻或文档到知识库，或者等待RSS自动采集任务完成。";
            } else {
                List<String> context = entries.stream()
                        .map(e -> e.getTitle() + "\n" + e.getContent())
                        .collect(Collectors.toList());
                
                log.debug("调用LLM生成回答，上下文文档数: {}", context.size());
                answer = ollamaClient.generateAnswer(request.getQuery(), context);
            }
        }

        // Step 6: 保存查询历史
        long responseTime = System.currentTimeMillis() - startTime;
        UserQueryHistory history = saveQueryHistory(
            userId,
            request,
            answer,
            vectorIds,
            searchResults.stream().map(VectorStore.SearchResult::getScore).collect(Collectors.toList()),
            responseTime
        );

        // Step 7: 构建响应
        QueryResponse response = new QueryResponse();
        response.setAnswer(answer);
        response.setRetrievedEntries(retrievedEntries);
        response.setResponseTimeMs(responseTime);
        response.setQueryId(history.getId());
        response.setSessionId(request.getSessionId());

        log.info("查询完成: QueryID={}, ResponseTime={}ms, RetrievedDocs={}", 
                history.getId(), responseTime, entries.size());

        return response;
    }

    @Override
    public QueryResponse semanticSearch(QueryRequest request, Long userId) throws IOException {
        request.setNeedAnswer(false);
        return query(request, userId);
    }

    /**
     * 保存查询历史
     */
    private UserQueryHistory saveQueryHistory(
            Long userId,
            QueryRequest request,
            String answer,
            List<Long> vectorIds,
            List<Double> scores,
            long responseTime
    ) {
        UserQueryHistory history = new UserQueryHistory()
                .setUserId(userId)
                .setQueryText(request.getQuery())
                .setQueryType(request.getQueryType())
                .setAnswerText(answer)
                .setRetrievedEntryIds(JSON.toJSONString(vectorIds))
                .setSimilarityScores(JSON.toJSONString(scores))
                .setResponseTimeMs((int) responseTime);

        queryHistoryMapper.insert(history);
        return history;
    }
}

