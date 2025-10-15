package com.xu.news.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 智能问答响应DTO
 * 
 * @author XU
 * @since 2025-10-15
 */
@Data
public class QueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI生成的回答
     */
    private String answer;

    /**
     * 相关知识条目列表
     */
    private List<RetrievedEntry> retrievedEntries;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTimeMs;

    /**
     * 查询ID（用于反馈）
     */
    private Long queryId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 检索到的知识条目
     */
    @Data
    public static class RetrievedEntry implements Serializable {
        
        private static final long serialVersionUID = 1L;

        /**
         * 条目ID
         */
        private Long id;

        /**
         * 标题
         */
        private String title;

        /**
         * 摘要或片段
         */
        private String summary;

        /**
         * 来源名称
         */
        private String sourceName;

        /**
         * 来源URL
         */
        private String sourceUrl;

        /**
         * 相似度得分
         */
        private Double similarityScore;

        /**
         * 发布时间
         */
        private String publishedAt;

        /**
         * 标签
         */
        private List<String> tags;
    }
}

