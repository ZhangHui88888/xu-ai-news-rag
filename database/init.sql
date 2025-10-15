-- XU-News-AI-RAG 数据库初始化脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS xu_news_rag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xu_news_rag;

-- ===========================
-- 用户表
-- ===========================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `full_name` VARCHAR(100) COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin/user',
    `preferences` JSON COMMENT '用户偏好',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ===========================
-- 知识条目表
-- ===========================
CREATE TABLE IF NOT EXISTS `knowledge_entry` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '知识条目ID',
    `title` VARCHAR(500) NOT NULL COMMENT '标题',
    `content` LONGTEXT NOT NULL COMMENT '内容',
    `summary` TEXT COMMENT 'AI生成的摘要',
    `source_id` BIGINT COMMENT '数据源ID',
    `source_name` VARCHAR(200) COMMENT '来源名称',
    `source_url` VARCHAR(1000) COMMENT '来源URL',
    `author` VARCHAR(200) COMMENT '作者',
    `published_at` DATETIME COMMENT '发布时间',
    `file_path` VARCHAR(500) COMMENT '文件路径',
    `vector_id` BIGINT COMMENT '向量ID',
    `vector_embedding` LONGTEXT COMMENT '向量嵌入',
    `tags` JSON COMMENT '标签数组',
    `content_type` VARCHAR(50) DEFAULT 'news' COMMENT '内容类型: news/article/document/other',
    `language` VARCHAR(10) DEFAULT 'zh-CN' COMMENT '语言',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布 2-已归档',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `created_by` BIGINT COMMENT '创建者ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    INDEX `idx_source_id` (`source_id`),
    INDEX `idx_vector_id` (`vector_id`),
    INDEX `idx_content_type` (`content_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_published_at` (`published_at`),
    INDEX `idx_created_at` (`created_at`),
    FULLTEXT INDEX `idx_fulltext` (`title`, `content`) WITH PARSER ngram,
    FOREIGN KEY (`source_id`) REFERENCES `data_source`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`created_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识条目表';

-- ===========================
-- 数据源表
-- ===========================
CREATE TABLE IF NOT EXISTS `data_source` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '数据源ID',
    `name` VARCHAR(200) NOT NULL COMMENT '数据源名称',
    `source_type` VARCHAR(50) NOT NULL COMMENT '类型: rss/web/file/api',
    `source_url` VARCHAR(1000) COMMENT '数据源URL',
    `n8n_workflow_id` VARCHAR(100) COMMENT 'n8n工作流ID',
    `fetch_frequency` VARCHAR(100) COMMENT '抓取频率(cron)',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    `config` JSON COMMENT '配置信息',
    `last_fetch_at` DATETIME COMMENT '最后抓取时间',
    `last_fetch_status` VARCHAR(50) COMMENT '最后抓取状态: success/failed/pending',
    `last_fetch_error` TEXT COMMENT '最后抓取错误信息',
    `total_entries` INT NOT NULL DEFAULT 0 COMMENT '抓取的条目总数',
    `created_by` BIGINT COMMENT '创建者ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    INDEX `idx_source_type` (`source_type`),
    INDEX `idx_enabled` (`enabled`),
    FOREIGN KEY (`created_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源表';

-- ===========================
-- 标签表
-- ===========================
CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    `name` VARCHAR(100) NOT NULL UNIQUE COMMENT '标签名称',
    `color` VARCHAR(20) COMMENT '标签颜色',
    `description` VARCHAR(500) COMMENT '标签描述',
    `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `created_by` BIGINT COMMENT '创建者ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    INDEX `idx_name` (`name`),
    INDEX `idx_usage_count` (`usage_count`),
    FOREIGN KEY (`created_by`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ===========================
-- 用户查询历史表
-- ===========================
CREATE TABLE IF NOT EXISTS `user_query_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '查询历史ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `query_text` TEXT NOT NULL COMMENT '查询问题',
    `query_type` VARCHAR(50) NOT NULL COMMENT '查询类型: keyword/semantic/hybrid/chat',
    `answer_text` LONGTEXT COMMENT 'AI生成的回答',
    `retrieved_entry_ids` JSON COMMENT '检索到的知识条目ID列表',
    `similarity_scores` JSON COMMENT '相似度得分列表',
    `response_time_ms` INT COMMENT '响应时间(毫秒)',
    `user_feedback` TINYINT COMMENT '用户反馈: 1-好评 0-中评 -1-差评',
    `feedback_comment` TEXT COMMENT '用户反馈评论',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '查询时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_query_type` (`query_type`),
    INDEX `idx_created_at` (`created_at`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户查询历史表';

-- ===========================
-- 系统配置表
-- ===========================
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    `config_key` VARCHAR(200) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `config_group` VARCHAR(100) COMMENT '配置分组',
    `description` VARCHAR(500) COMMENT '配置描述',
    `value_type` VARCHAR(50) DEFAULT 'string' COMMENT '数据类型: string/number/boolean/json',
    `is_encrypted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否加密: 0-否 1-是',
    `updated_by` BIGINT COMMENT '更新者ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ===========================
-- 初始化数据
-- ===========================

-- 插入管理员账户 (密码: admin123)
INSERT INTO `user` (`username`, `email`, `password_hash`, `full_name`, `role`, `status`) 
VALUES ('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin', 1);

-- 插入测试用户 (密码: user123)
INSERT INTO `user` (`username`, `email`, `password_hash`, `full_name`, `role`, `status`) 
VALUES ('user', 'user@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '测试用户', 'user', 1);

-- 插入初始标签
INSERT INTO `tag` (`name`, `color`, `description`) VALUES
('AI', '#409EFF', '人工智能相关'),
('机器学习', '#67C23A', '机器学习技术'),
('深度学习', '#E6A23C', '深度学习'),
('NLP', '#F56C6C', '自然语言处理'),
('计算机视觉', '#909399', '计算机视觉'),
('开源', '#409EFF', '开源项目');

-- 插入系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_group`, `description`, `value_type`) VALUES
('system.name', 'XU-News-AI-RAG', 'system', '系统名称', 'string'),
('system.version', '1.0.0', 'system', '系统版本', 'string'),
('embedding.model', 'all-MiniLM-L6-v2', 'ai', '嵌入模型', 'string'),
('llm.model', 'qwen2.5:3b', 'ai', 'LLM模型', 'string'),
('rag.topk', '5', 'rag', '检索Top K', 'number'),
('rag.similarity_threshold', '0.5', 'rag', '相似度阈值', 'number');

-- 完成
SELECT 'Database initialization completed successfully!' AS status;

