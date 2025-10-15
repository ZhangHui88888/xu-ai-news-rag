package com.xu.news.controller;

import com.xu.news.common.Result;
import com.xu.news.dto.LoginRequest;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.entity.User;
import com.xu.news.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * @author XU
 * @since 2025-10-15
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            
            return Result.success("注册成功", data);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenType", "Bearer");
            
            return Result.success("登录成功", data);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.unauthorized(e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("XU-News-AI-RAG is running!");
    }
}

