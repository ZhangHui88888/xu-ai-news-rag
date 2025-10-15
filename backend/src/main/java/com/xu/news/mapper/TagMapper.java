package com.xu.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xu.news.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签Mapper
 * 
 * @author XU
 * @since 2025-10-15
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 获取热门标签
     */
    List<Tag> getHotTags(@Param("limit") Integer limit);
    
    /**
     * 增加标签使用次数
     */
    int incrementUsageCount(@Param("name") String name);
}

