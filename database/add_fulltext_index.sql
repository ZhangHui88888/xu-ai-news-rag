-- 添加FULLTEXT索引以支持全文搜索（可选）
-- 注意：这个脚本是可选的，当前系统使用LIKE查询，不需要FULLTEXT索引
-- 如果数据量很大且需要更快的搜索性能，可以执行此脚本

USE xu_news_rag;

-- 为knowledge_entry表的title和content字段添加FULLTEXT索引
ALTER TABLE knowledge_entry 
ADD FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram;

-- 验证索引是否创建成功
SHOW INDEX FROM knowledge_entry WHERE Key_name = 'ft_title_content';

-- 使用说明：
-- 1. 执行此脚本后，需要修改KnowledgeEntryMapper.xml，恢复MATCH...AGAINST查询
-- 2. FULLTEXT索引适合大数据量场景（> 10万条记录）
-- 3. 如果数据量较小，使用LIKE查询即可，不需要此索引

