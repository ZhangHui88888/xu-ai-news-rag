package com.xu.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xu.news.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置Mapper
 * 
 * @author XU
 * @since 2025-10-15
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询配置
     */
    SystemConfig findByKey(@Param("configKey") String configKey);
}

