-- 在Navicat中执行此SQL更新表结构
-- 数据库: xu_news_rag

USE xu_news_rag;

-- 添加缺失的字段
ALTER TABLE user_query_history 
  ADD COLUMN answer_text LONGTEXT COMMENT 'AI生成的回答' AFTER query_type;

ALTER TABLE user_query_history 
  ADD COLUMN retrieved_entry_ids JSON COMMENT '检索到的知识条目ID列表' AFTER answer_text;

ALTER TABLE user_query_history 
  ADD COLUMN similarity_scores JSON COMMENT '相似度得分列表' AFTER retrieved_entry_ids;

ALTER TABLE user_query_history 
  ADD COLUMN response_time_ms INT COMMENT '响应时间(毫秒)' AFTER similarity_scores;

ALTER TABLE user_query_history 
  ADD COLUMN user_feedback TINYINT COMMENT '用户反馈: 1-好评 0-中评 -1-差评' AFTER response_time_ms;

ALTER TABLE user_query_history 
  ADD COLUMN feedback_comment TEXT COMMENT '用户反馈评论' AFTER user_feedback;

ALTER TABLE user_query_history 
  ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除' AFTER feedback_comment;

-- 删除不需要的旧字段（如果存在会报错，忽略即可）
-- ALTER TABLE user_query_history DROP COLUMN result_count;
-- ALTER TABLE user_query_history DROP COLUMN response_time;
-- ALTER TABLE user_query_history DROP COLUMN is_satisfied;
-- ALTER TABLE user_query_history DROP COLUMN feedback;

-- 查看最终表结构
DESCRIBE user_query_history;

SELECT '✅ 表结构更新完成！' AS status;

