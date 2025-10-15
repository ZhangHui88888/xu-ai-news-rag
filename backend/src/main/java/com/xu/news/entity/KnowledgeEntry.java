package com.xu.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识条目实体
 * 
 * @author XU
 * @since 2025-10-15
 */
@Data
@Accessors(chain = true)
@TableName("knowledge_entry")
public class KnowledgeEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 知识条目ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容/正文
     */
    @TableField("content")
    private String content;

    /**
     * AI生成的摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 数据源ID
     */
    @TableField("source_id")
    private Long sourceId;

    /**
     * 来源名称
     */
    @TableField("source_name")
    private String sourceName;

    /**
     * 来源URL
     */
    @TableField("source_url")
    private String sourceUrl;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 发布时间
     */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    /**
     * 文件路径（如果是上传的文档）
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 向量ID（FAISS中的索引ID）
     */
    @TableField("vector_id")
    private Long vectorId;

    /**
     * 向量嵌入（base64编码的二进制数据，或JSON数组）
     */
    @TableField("vector_embedding")
    private String vectorEmbedding;

    /**
     * 标签（JSON数组，如 ["AI", "机器学习"]）
     */
    @TableField("tags")
    private String tags;

    /**
     * 内容类型：news/article/document/other
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 语言：zh-CN/en-US
     */
    @TableField("language")
    private String language;

    /**
     * 状态：0-草稿 1-已发布 2-已归档
     */
    @TableField("status")
    private Integer status;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

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

