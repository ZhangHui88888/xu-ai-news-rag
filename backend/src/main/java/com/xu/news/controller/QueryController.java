package com.xu.news.controller;

import com.xu.news.common.Result;
import com.xu.news.dto.QueryRequest;
import com.xu.news.dto.QueryResponse;
import com.xu.news.service.QueryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 智能问答控制器
 * 
 * @author XU
 * @since 2025-10-15
 */
@Slf4j
@RestController
@RequestMapping("/query")
public class QueryController {

    @Autowired
    private QueryService queryService;

    /**
     * 智能问答（RAG）
     */
    @PostMapping("/ask")
    public Result<QueryResponse> ask(@Valid @RequestBody QueryRequest request, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            QueryResponse response = queryService.query(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 语义搜索
     */
    @PostMapping("/search")
    public Result<QueryResponse> search(@Valid @RequestBody QueryRequest request, Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            QueryResponse response = queryService.semanticSearch(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }
}

