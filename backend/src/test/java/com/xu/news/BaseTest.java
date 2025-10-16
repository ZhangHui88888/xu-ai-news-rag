package com.xu.news;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 测试基类
 * 
 * @author XU
 * @since 2025-10-16
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public abstract class BaseTest {
    
    /**
     * 测试数据准备方法
     * 子类可以重写此方法来准备测试数据
     */
    protected void prepareTestData() {
        // 子类实现
    }
    
    /**
     * 测试数据清理方法
     * 子类可以重写此方法来清理测试数据
     */
    protected void cleanTestData() {
        // 子类实现
    }
}

