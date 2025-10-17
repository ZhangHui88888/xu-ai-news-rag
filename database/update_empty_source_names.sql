-- 更新现有的空来源名称
-- 将所有来源为空的记录更新为"n8n自动读取"

USE xu_news_rag;

-- 查看当前有多少条记录的来源为空
SELECT 
    COUNT(*) as empty_source_count,
    content_type,
    COUNT(*) as count_by_type
FROM knowledge_entry 
WHERE (source_name IS NULL OR source_name = '') 
  AND deleted = 0
GROUP BY content_type;

-- 更新所有来源为空的记录
UPDATE knowledge_entry 
SET source_name = 'n8n自动读取'
WHERE (source_name IS NULL OR source_name = '') 
  AND deleted = 0;

-- 验证更新结果
SELECT 
    source_name,
    COUNT(*) as count
FROM knowledge_entry 
WHERE deleted = 0
GROUP BY source_name
ORDER BY count DESC;

-- 显示更新后的记录样本
SELECT 
    id,
    title,
    source_name,
    content_type,
    created_at
FROM knowledge_entry 
WHERE source_name = 'n8n自动读取'
  AND deleted = 0
ORDER BY created_at DESC
LIMIT 10;

