package com.xu.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xu.news.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 * 
 * @author XU
 * @since 2025-10-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(@Param("email") String email);
}

