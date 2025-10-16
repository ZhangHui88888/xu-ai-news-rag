USE xu_news_rag;

-- Knowledge Entry Table
CREATE TABLE IF NOT EXISTS `knowledge_entry` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(500) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `summary` TEXT,
    `source_id` BIGINT,
    `source_name` VARCHAR(200),
    `source_url` VARCHAR(1000),
    `author` VARCHAR(200),
    `published_at` DATETIME,
    `file_path` VARCHAR(500),
    `vector_id` BIGINT,
    `vector_embedding` LONGTEXT,
    `tags` JSON,
    `content_type` VARCHAR(50) DEFAULT 'news',
    `language` VARCHAR(10) DEFAULT 'zh-CN',
    `status` TINYINT(1) NOT NULL DEFAULT 1,
    `view_count` INT NOT NULL DEFAULT 0,
    `created_by` BIGINT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    INDEX `idx_title` (`title`(100)),
    INDEX `idx_source_url` (`source_url`(255)),
    INDEX `idx_content_type` (`content_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data Source Table
CREATE TABLE IF NOT EXISTS `data_source` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `url` VARCHAR(1000),
    `config` JSON,
    `status` TINYINT(1) NOT NULL DEFAULT 1,
    `last_fetch_at` DATETIME,
    `created_by` BIGINT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tag Table
CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(500),
    `usage_count` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User Query History Table
CREATE TABLE IF NOT EXISTS `user_query_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `query_text` TEXT NOT NULL,
    `query_type` VARCHAR(50) DEFAULT 'search',
    `result_count` INT,
    `response_time` INT,
    `is_satisfied` TINYINT(1),
    `feedback` TEXT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System Config Table
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(100) NOT NULL UNIQUE,
    `config_value` TEXT,
    `description` VARCHAR(500),
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Tables created successfully!' AS Status;

