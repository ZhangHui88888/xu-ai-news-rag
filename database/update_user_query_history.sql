-- 更新 user_query_history 表结构
-- 添加缺失的字段以匹配 UserQueryHistory 实体类

USE xu_news_rag;

-- 先检查并删除旧字段
ALTER TABLE user_query_history DROP COLUMN IF EXISTS result_count;
ALTER TABLE user_query_history DROP COLUMN IF EXISTS response_time;
ALTER TABLE user_query_history DROP COLUMN IF EXISTS is_satisfied;
ALTER TABLE user_query_history DROP COLUMN IF EXISTS feedback;

-- 添加新字段（如果字段已存在，忽略错误）
SET @sql1 = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'user_query_history' 
    AND TABLE_SCHEMA = 'xu_news_rag' 
    AND COLUMN_NAME = 'answer_text') = 0,
    'ALTER TABLE user_query_history ADD COLUMN answer_text LONGTEXT COMMENT ''AI生成的回答'' AFTER query_type',
    'SELECT ''Column answer_text already exists'' AS result');
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @sql2 = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'user_query_history' 
    AND TABLE_SCHEMA = 'xu_news_rag' 
    AND COLUMN_NAME = 'retrieved_entry_ids') = 0,
    'ALTER TABLE user_query_history ADD COLUMN retrieved_entry_ids JSON COMMENT ''检索到的知识条目ID列表'' AFTER answer_text',
    'SELECT ''Column retrieved_entry_ids already exists'' AS result');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SET @sql3 = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'user_query_history' 
    AND TABLE_SCHEMA = 'xu_news_rag' 
    AND COLUMN_NAME = 'similarity_scores') = 0,
    'ALTER TABLE user_query_history ADD COLUMN similarity_scores JSON COMMENT ''相似度得分列表'' AFTER retrieved_entry_ids',
    'SELECT ''Column similarity_scores already exists'' AS result');
PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

SET @sql4 = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'user_query_history' 
    AND TABLE_SCHEMA = 'xu_news_rag' 
    AND COLUMN_NAME = 'response_time_ms') = 0,
    'ALTER TABLE user_query_history ADD COLUMN response_time_ms INT COMMENT ''响应时间(毫秒)'' AFTER similarity_scores',
    'SELECT ''Column response_time_ms already exists'' AS result');
PREPARE stmt4 FROM @sql4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

SET @sql5 = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'user_query_history' 
    AND TABLE_SCHEMA = 'xu_news_rag' 
    AND COLUMN_NAME = 'user_feedback') = 0,
    'ALTER TABLE user_query_history ADD COLUMN user_feedback TINYINT COMMENT ''用户反馈: 1-好评 0-中评 -1-差评'' AFTER response_time_ms',
    'SELECT ''Column user_feedback already exists'' AS result');
PREPARE stmt5 FROM @sql5;
EXECUTE stmt5;
DEALLOCATE PREPARE stmt5;

SET @sql6 = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'user_query_history' 
    AND TABLE_SCHEMA = 'xu_news_rag' 
    AND COLUMN_NAME = 'feedback_comment') = 0,
    'ALTER TABLE user_query_history ADD COLUMN feedback_comment TEXT COMMENT ''用户反馈评论'' AFTER user_feedback',
    'SELECT ''Column feedback_comment already exists'' AS result');
PREPARE stmt6 FROM @sql6;
EXECUTE stmt6;
DEALLOCATE PREPARE stmt6;

SET @sql7 = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'user_query_history' 
    AND TABLE_SCHEMA = 'xu_news_rag' 
    AND COLUMN_NAME = 'deleted') = 0,
    'ALTER TABLE user_query_history ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''逻辑删除'' AFTER feedback_comment',
    'SELECT ''Column deleted already exists'' AS result');
PREPARE stmt7 FROM @sql7;
EXECUTE stmt7;
DEALLOCATE PREPARE stmt7;

-- 验证表结构
DESCRIBE user_query_history;

SELECT '✅ user_query_history 表结构更新完成！' AS status;

