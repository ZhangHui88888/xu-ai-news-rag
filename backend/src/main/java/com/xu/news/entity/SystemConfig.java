package com.xu.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体
 * 
 * @author XU
 * @since 2025-10-15
 */
@Data
@Accessors(chain = true)
@TableName("system_config")
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置分组
     */
    @TableField("config_group")
    private String configGroup;

    /**
     * 配置描述
     */
    @TableField("description")
    private String description;

    /**
     * 数据类型：string/number/boolean/json
     */
    @TableField("value_type")
    private String valueType;

    /**
     * 是否加密：0-否 1-是
     */
    @TableField("is_encrypted")
    private Integer isEncrypted;

    /**
     * 更新者ID
     */
    @TableField("updated_by")
    private Long updatedBy;

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
}

