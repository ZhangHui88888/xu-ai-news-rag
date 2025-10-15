package com.xu.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xu.news.entity.KnowledgeEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识条目Mapper
 * 
 * @author XU
 * @since 2025-10-15
 */
@Mapper
public interface KnowledgeEntryMapper extends BaseMapper<KnowledgeEntry> {

    /**
     * 全文搜索
     */
    IPage<KnowledgeEntry> fullTextSearch(
        Page<?> page,
        @Param("keyword") String keyword,
        @Param("sourceIds") List<Long> sourceIds,
        @Param("tags") List<String> tags,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    /**
     * 根据向量ID列表查询
     */
    List<KnowledgeEntry> findByVectorIds(@Param("vectorIds") List<Long> vectorIds);

    /**
     * 增加浏览次数
     */
    int incrementViewCount(@Param("id") Long id);
}

