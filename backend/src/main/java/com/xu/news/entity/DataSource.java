package com.xu.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据源实体
 * 
 * @author XU
 * @since 2025-10-15
 */
@Data
@Accessors(chain = true)
@TableName("data_source")
public class DataSource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 数据源名称
     */
    @TableField("name")
    private String name;

    /**
     * 数据源类型：rss/web/file/api
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 数据源URL（RSS链接或网页URL）
     */
    @TableField("source_url")
    private String sourceUrl;

    /**
     * n8n工作流ID
     */
    @TableField("n8n_workflow_id")
    private String n8nWorkflowId;

    /**
     * 抓取频率（cron表达式）
     */
    @TableField("fetch_frequency")
    private String fetchFrequency;

    /**
     * 是否启用：0-禁用 1-启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 配置（JSON格式）
     */
    @TableField("config")
    private String config;

    /**
     * 最后抓取时间
     */
    @TableField("last_fetch_at")
    private LocalDateTime lastFetchAt;

    /**
     * 最后抓取状态：success/failed/pending
     */
    @TableField("last_fetch_status")
    private String lastFetchStatus;

    /**
     * 最后抓取错误信息
     */
    @TableField("last_fetch_error")
    private String lastFetchError;

    /**
     * 抓取的条目总数
     */
    @TableField("total_entries")
    private Integer totalEntries;

    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

