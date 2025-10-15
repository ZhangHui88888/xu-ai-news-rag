package com.xu.news.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 智能问答请求DTO
 * 
 * @author XU
 * @since 2025-10-15
 */
@Data
public class QueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String query;

    /**
     * 查询类型：semantic（语义）/ keyword（关键词）/ hybrid（混合）
     */
    private String queryType = "semantic";

    /**
     * 返回结果数量
     */
    private Integer topK = 5;

    /**
     * 相似度阈值（0-1）
     */
    private Double similarityThreshold = 0.5;

    /**
     * 是否需要AI生成回答
     */
    private Boolean needAnswer = true;

    /**
     * 会话ID（用于上下文对话）
     */
    private String sessionId;
}

