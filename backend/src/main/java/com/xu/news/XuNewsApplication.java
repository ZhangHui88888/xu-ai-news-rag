package com.xu.news;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * XU-News-AI-RAG 应用入口
 * 
 * @author XU
 * @since 2025-10-15
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.xu.news.mapper")
public class XuNewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuNewsApplication.class, args);
        System.out.println("""
            
            ====================================
            🚀 XU-News-AI-RAG Started Successfully!
            📰 个性化新闻智能知识库系统
            🌐 API: http://localhost:8080/api
            📚 Docs: http://localhost:8080/api/doc.html
            ====================================
            """);
    }
}

