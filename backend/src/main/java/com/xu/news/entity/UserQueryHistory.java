package com.xu.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户查询历史实体
 * 
 * @author XU
 * @since 2025-10-15
 */
@Data
@Accessors(chain = true)
@TableName("user_query_history")
public class UserQueryHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 查询历史ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户查询问题
     */
    @TableField("query_text")
    private String queryText;

    /**
     * 查询类型：keyword/semantic/hybrid/chat
     */
    @TableField("query_type")
    private String queryType;

    /**
     * AI生成的回答
     */
    @TableField("answer_text")
    private String answerText;

    /**
     * 检索到的知识条目ID列表（JSON数组）
     */
    @TableField("retrieved_entry_ids")
    private String retrievedEntryIds;

    /**
     * 相似度得分（JSON数组）
     */
    @TableField("similarity_scores")
    private String similarityScores;

    /**
     * 响应时间（毫秒）
     */
    @TableField("response_time_ms")
    private Integer responseTimeMs;

    /**
     * 用户反馈：1-好评 0-中评 -1-差评
     */
    @TableField("user_feedback")
    private Integer userFeedback;

    /**
     * 用户反馈评论
     */
    @TableField("feedback_comment")
    private String feedbackComment;

    /**
     * 创建时间（查询时间）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

