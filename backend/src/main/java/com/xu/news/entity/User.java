package com.xu.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 
 * @author XU
 * @since 2025-10-15
 */
@Data
@Accessors(chain = true)
@TableName(value = "user", autoResultMap = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 密码哈希（BCrypt）
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 真实姓名
     */
    @TableField("full_name")
    private String fullName;

    /**
     * 用户角色：admin/user
     */
    @TableField("role")
    private String role;

    /**
     * 用户偏好（JSON格式）
     */
    @TableField("preferences")
    private String preferences;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 账户状态：0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

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

