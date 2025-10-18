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
import com.xu.news.util.RerankerClient;
import com.xu.news.util.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private RerankerClient rerankerClient;

    @Value("${reranker.enabled:false}")
    private Boolean rerankerEnabled;

    @Value("${reranker.candidate-multiplier:4}")
    private Integer candidateMultiplier;

    @Override
    public QueryResponse query(QueryRequest request, Long userId) throws IOException {
        long startTime = System.currentTimeMillis();

        // Step 1: 将问题向量化
        log.debug("生成查询向量: {}", request.getQuery());
        List<Double> queryVector = ollamaClient.generateEmbedding(request.getQuery());

        // Step 2: 在向量库中检索相关文档（召回阶段）
        // 如果启用重排，则检索更多候选文档
        int candidateCount = rerankerEnabled ? 
            request.getTopK() * candidateMultiplier : request.getTopK();
        
        log.debug("检索相关文档，CandidateCount={}, Threshold={}", 
            candidateCount, request.getSimilarityThreshold());
        
        List<VectorStore.SearchResult> searchResults = vectorStore.searchWithThreshold(
            queryVector,
            candidateCount,
            request.getSimilarityThreshold()
        );

        // Step 3: 获取候选文档详情
        List<Long> vectorIds = searchResults.stream()
                .map(VectorStore.SearchResult::getVectorId)
                .collect(Collectors.toList());
        
        final List<KnowledgeEntry> candidateEntries;
        if (!vectorIds.isEmpty()) {
            candidateEntries = knowledgeEntryService.findByVectorIds(vectorIds);
        } else {
            candidateEntries = new ArrayList<>();
        }

        // Step 4: 重排序（精排阶段）
        List<KnowledgeEntry> entries;
        List<Double> finalScores = searchResults.stream()
                .map(VectorStore.SearchResult::getScore)
                .collect(Collectors.toList());
        
        if (rerankerEnabled && !candidateEntries.isEmpty()) {
            try {
                log.debug("执行重排序: 候选文档数={}, 目标TopK={}", 
                    candidateEntries.size(), request.getTopK());
                
                // 准备文档内容用于重排
                List<String> documents = candidateEntries.stream()
                        .map(e -> e.getTitle() + "\n" + e.getContent())
                        .collect(Collectors.toList());
                
                // 调用重排模型
                List<RerankerClient.RerankResult> rerankResults = 
                    rerankerClient.rerank(request.getQuery(), documents, request.getTopK());
                
                // 根据重排结果重新排序文档
                entries = rerankResults.stream()
                        .map(r -> candidateEntries.get(r.getIndex()))
                        .collect(Collectors.toList());
                
                // 使用重排分数
                finalScores = rerankResults.stream()
                        .map(RerankerClient.RerankResult::getRelevanceScore)
                        .collect(Collectors.toList());
                
                log.info("重排完成: 输入候选数={}, 输出结果数={}", 
                    candidateEntries.size(), entries.size());
                
            } catch (Exception e) {
                log.error("重排失败，使用原始向量检索结果: {}", e.getMessage());
                // 重排失败时回退到向量检索结果
                entries = candidateEntries.stream()
                        .limit(request.getTopK())
                        .collect(Collectors.toList());
            }
        } else if (!candidateEntries.isEmpty()) {
            // 如果未启用重排，只取 TopK
            entries = candidateEntries.stream()
                    .limit(request.getTopK())
                    .collect(Collectors.toList());
        } else {
            entries = candidateEntries;
        }

        // Step 5: 构建响应的检索结果
        List<QueryResponse.RetrievedEntry> retrievedEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            KnowledgeEntry entry = entries.get(i);
            Double score = i < finalScores.size() ? finalScores.get(i) : 0.0;
            
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

        // Step 6: 使用LLM生成回答（如果需要）
        String answer = null;
        if (request.getNeedAnswer()) {
            if (entries.isEmpty()) {
                // 知识库中没有相关内容，直接用LLM回答
                log.info("知识库中未找到相关内容，使用LLM直接回答用户问题");
                
                // 构建提示词，告诉大模型直接回答问题
                String prompt = String.format(
                    "请回答以下问题。如果你不确定答案，请基于你的知识尽可能提供有用的信息。\n\n" +
                    "问题：%s\n\n" +
                    "注意：这个问题在知识库中没有找到相关内容，请基于你的通用知识回答。",
                    request.getQuery()
                );
                
                answer = ollamaClient.generateAnswer(prompt);
                
                // 在答案末尾添加提示
                answer = answer + "\n\n---\n💡 提示：以上回答基于大模型的通用知识生成。" +
                         "如需更准确的信息，建议导入相关文档到知识库。";
            } else {
                // 知识库中有相关内容，基于检索结果生成回答
                List<String> context = entries.stream()
                        .map(e -> e.getTitle() + "\n" + e.getContent())
                        .collect(Collectors.toList());
                
                log.debug("调用LLM生成回答，上下文文档数: {}", context.size());
                answer = ollamaClient.generateAnswer(request.getQuery(), context);
            }
        }

        // Step 7: 保存查询历史
        long responseTime = System.currentTimeMillis() - startTime;
        
        // 保存最终使用的文档ID
        List<Long> finalVectorIds = entries.stream()
                .map(KnowledgeEntry::getVectorId)
                .collect(Collectors.toList());
        
        UserQueryHistory history = saveQueryHistory(
            userId,
            request,
            answer,
            finalVectorIds,
            finalScores,
            responseTime
        );

        // Step 8: 构建响应
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

