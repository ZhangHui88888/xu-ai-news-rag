-- H2 测试数据库初始化脚本（MODE=MySQL）
-- 
-- 注意：
-- 1. 使用 MODE=MySQL 模式，支持 MySQL 语法
-- 2. 字段长度与生产环境（MySQL）保持一致
-- 3. LONGTEXT 在 MODE=MySQL 下被支持，自动映射到 CLOB
-- 4. JSON → TEXT（H2 不完全支持 JSON 类型）
-- 5. 外键和全文索引在测试环境中省略（简化测试）
--
-- @author XU
-- @since 2025-10-18
-- @updated 2025-10-18 - 字段长度统一，类型调整为 MySQL 兼容

-- 用户表（user是保留字，需要用引号）
CREATE TABLE IF NOT EXISTS "user" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    preferences VARCHAR(2000),
    last_login_at DATETIME,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 数据源表
CREATE TABLE IF NOT EXISTS data_source (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_url VARCHAR(1000) NOT NULL,
    n8n_workflow_id VARCHAR(100),
    fetch_frequency VARCHAR(100),
    enabled TINYINT DEFAULT 1,
    config TEXT,
    last_fetch_at DATETIME,
    last_fetch_status VARCHAR(50),
    last_fetch_error TEXT,
    total_entries INT DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 知识库条目表
CREATE TABLE IF NOT EXISTS knowledge_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content LONGTEXT NOT NULL,
    summary TEXT,
    source_id BIGINT,
    source_name VARCHAR(200),
    source_url VARCHAR(1000),
    author VARCHAR(200),
    published_at DATETIME,
    file_path VARCHAR(500),
    vector_id BIGINT,
    vector_embedding LONGTEXT,
    tags TEXT,
    content_type VARCHAR(50),
    language VARCHAR(10),
    status TINYINT DEFAULT 1,
    view_count INT DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(20),
    description VARCHAR(500),
    usage_count INT DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 用户查询历史表
CREATE TABLE IF NOT EXISTS user_query_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    query_text TEXT NOT NULL,
    query_type VARCHAR(50),
    answer_text LONGTEXT,
    retrieved_entry_ids TEXT,
    similarity_scores TEXT,
    response_time_ms INT,
    user_feedback TINYINT,
    feedback_comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(200) NOT NULL UNIQUE,
    config_value TEXT,
    config_group VARCHAR(100),
    description VARCHAR(500),
    value_type VARCHAR(50) DEFAULT 'string',
    is_encrypted TINYINT DEFAULT 0,
    updated_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
