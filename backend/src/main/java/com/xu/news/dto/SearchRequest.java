package com.xu.news.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索请求DTO
 * 
 * @author XU
 * @since 2025-10-15
 */
@Data
public class SearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索类型：keyword/semantic/hybrid
     */
    private String searchType = "hybrid";

    /**
     * 来源筛选
     */
    private List<Long> sourceIds;

    /**
     * 标签筛选
     */
    private List<String> tags;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 排序字段：relevance/time/views
     */
    private String sortBy = "relevance";

    /**
     * 排序方向：asc/desc
     */
    private String sortOrder = "desc";

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 20L;
}

