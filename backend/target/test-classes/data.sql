-- H2 测试数据初始化

-- 清空表数据
DELETE FROM user_query_history;
DELETE FROM knowledge_entry;
DELETE FROM tag;
DELETE FROM data_source;
DELETE FROM "user";
DELETE FROM system_config;

-- 插入测试用户（注意：user 是保留字，需要加引号；字段是 password_hash 不是 password）
INSERT INTO "user" (id, username, password_hash, email, full_name, role, status, created_at, updated_at, deleted) 
VALUES (1, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@example.com', '测试用户', 'user', 1, NOW(), NOW(), 0);

INSERT INTO "user" (id, username, password_hash, email, full_name, role, status, created_at, updated_at, deleted) 
VALUES (2, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', '管理员', 'admin', 1, NOW(), NOW(), 0);

-- 插入测试数据源
INSERT INTO data_source (id, name, source_type, source_url, enabled, created_at, updated_at, deleted) 
VALUES (1, 'TechCrunch', 'RSS', 'https://techcrunch.com/feed/', 1, NOW(), NOW(), 0);

INSERT INTO data_source (id, name, source_type, source_url, enabled, created_at, updated_at, deleted) 
VALUES (2, 'The Verge', 'RSS', 'https://www.theverge.com/rss/index.xml', 1, NOW(), NOW(), 0);

-- 插入测试知识库条目
INSERT INTO knowledge_entry (id, title, content, summary, content_type, source_url, source_name, author, published_at, user_id, data_source_id, created_at, updated_at, deleted) 
VALUES (1, 'AI技术最新进展', '人工智能技术在2025年取得了重大突破...', 'AI在多个领域取得进展', 'news', 'https://example.com/ai-news-1', 'TechCrunch', 'John Doe', NOW(), 1, 1, NOW(), NOW(), 0);

INSERT INTO knowledge_entry (id, title, content, summary, content_type, source_url, source_name, author, published_at, user_id, data_source_id, created_at, updated_at, deleted) 
VALUES (2, 'RAG技术详解', '检索增强生成（RAG）是一种结合检索和生成的AI技术...', 'RAG技术介绍', 'article', 'https://example.com/rag-article', 'Tech Blog', 'Jane Smith', NOW(), 1, NULL, NOW(), NOW(), 0);

INSERT INTO knowledge_entry (id, title, content, summary, content_type, source_url, source_name, author, published_at, user_id, data_source_id, created_at, updated_at, deleted) 
VALUES (3, '机器学习基础', '机器学习是人工智能的一个分支，专注于让计算机从数据中学习...', '机器学习入门', 'tutorial', 'https://example.com/ml-tutorial', 'Learning Platform', 'Bob Wilson', NOW(), 2, NULL, NOW(), NOW(), 0);

-- 插入测试标签
INSERT INTO tag (id, name, color, created_at, updated_at, deleted) 
VALUES (1, 'AI', '#3b82f6', NOW(), NOW(), 0);

INSERT INTO tag (id, name, color, created_at, updated_at, deleted) 
VALUES (2, 'ML', '#10b981', NOW(), NOW(), 0);

INSERT INTO tag (id, name, color, created_at, updated_at, deleted) 
VALUES (3, 'RAG', '#f59e0b', NOW(), NOW(), 0);

-- 插入测试查询历史
INSERT INTO user_query_history (id, user_id, query_text, query_type, answer_text, response_time_ms, created_at, deleted) 
VALUES (1, 1, '什么是人工智能？', 'search', 'AI是计算机科学的一个分支...', 100, NOW(), 0);

INSERT INTO user_query_history (id, user_id, query_text, query_type, answer_text, response_time_ms, created_at, deleted) 
VALUES (2, 1, 'RAG技术的优势', 'search', 'RAG技术结合了检索和生成...', 150, NOW(), 0);

-- 插入系统配置
INSERT INTO system_config (id, config_key, config_value, description, created_at, updated_at, deleted) 
VALUES (1, 'system.version', '1.0.0', '系统版本号', NOW(), NOW(), 0);

INSERT INTO system_config (id, config_key, config_value, description, created_at, updated_at, deleted) 
VALUES (2, 'ai.model.llm', 'qwen3:0.6b', 'LLM模型名称', NOW(), NOW(), 0);

