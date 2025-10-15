package com.xu.news.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.entity.User;
import com.xu.news.mapper.UserMapper;
import com.xu.news.service.UserService;
import com.xu.news.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 
 * @author XU
 * @since 2025-10-15
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterRequest request) {
        // 验证确认密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        existingUser = userMapper.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User()
                .setUsername(request.getUsername())
                .setEmail(request.getEmail())
                .setPasswordHash(passwordEncoder.encode(request.getPassword()))
                .setFullName(request.getFullName())
                .setRole("user")
                .setStatus(1);

        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());

        return user;
    }

    @Override
    public String login(LoginRequest request) {
        // 查找用户（支持用户名或邮箱登录）
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            user = userMapper.findByEmail(request.getUsername());
        }

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查账户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }

        // 更新最后登录时间
        updateLastLoginTime(user.getId());

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户登录成功: {}", user.getUsername());

        return token;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLoginTime(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastLoginAt(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }
}

