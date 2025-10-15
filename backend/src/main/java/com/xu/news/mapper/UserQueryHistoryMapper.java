package com.xu.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xu.news.entity.UserQueryHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户查询历史Mapper
 * 
 * @author XU
 * @since 2025-10-15
 */
@Mapper
public interface UserQueryHistoryMapper extends BaseMapper<UserQueryHistory> {
}

