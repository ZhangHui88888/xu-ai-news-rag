package com.xu.news.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.entity.User;

/**
 * 用户服务接口
 * 
 * @author XU
 * @since 2025-10-15
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    User register(RegisterRequest request);

    /**
     * 用户登录
     */
    String login(LoginRequest request);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(String email);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);
}

