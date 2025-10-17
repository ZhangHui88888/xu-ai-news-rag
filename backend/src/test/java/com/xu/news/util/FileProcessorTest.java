package com.xu.news.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileProcessor 单元测试（不依赖Spring）
 * 
 * @author XU
 * @since 2025-10-17
 */
@DisplayName("文件处理器测试")
class FileProcessorTest {

    private FileProcessor fileProcessor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileProcessor = new FileProcessor();
    }

    @Test
    @DisplayName("提取TXT文本")
    void testExtractText_Txt() throws IOException {
        // Given
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "测试内容");

        // When
        String result = fileProcessor.extractText(file.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("测试内容"));
    }

    @Test
    @DisplayName("提取Markdown文本")
    void testExtractText_Markdown() throws IOException {
        // Given
        Path file = tempDir.resolve("test.md");
        Files.writeString(file, "# 标题\n测试内容");

        // When
        String result = fileProcessor.extractText(file.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("标题"));
    }

    @Test
    @DisplayName("清理文本")
    void testCleanText() {
        // When
        String result = fileProcessor.cleanText("  多余  空格  ");

        // Then
        assertNotNull(result);
        assertEquals("多余 空格", result);
    }

    @Test
    @DisplayName("清理空文本")
    void testCleanText_Empty() {
        // When
        String result1 = fileProcessor.cleanText("");
        String result2 = fileProcessor.cleanText(null);

        // Then
        assertEquals("", result1);
        assertEquals("", result2);
    }

    @Test
    @DisplayName("不支持的文件类型")
    void testExtractText_Unsupported() {
        // Given
        Path file = tempDir.resolve("test.exe");

        // When & Then
        assertThrows(IOException.class, () -> {
            fileProcessor.extractText(file.toFile());
        });
    }
}

