package com.xu.news.base;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 测试基类 - 所有Spring Boot集成测试的基类
 * 
 * @author XU
 * @since 2025-10-17
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseTest {
    
    /**
     * 子类可以在此基础上添加通用的测试方法
     */
}

