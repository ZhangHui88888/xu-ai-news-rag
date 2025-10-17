package com.xu.news.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileProcessor 单元测试
 * 
 * @author XU
 * @since 2025-10-17
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("文件处理器测试")
@Import(com.xu.news.config.TestConfiguration.class)
class FileProcessorTest {

    @Autowired
    private FileProcessor fileProcessor;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("提取文本 - TXT文件")
    void testExtractText_TxtFile() throws IOException {
        // Given
        Path txtFile = tempDir.resolve("test.txt");
        String content = "这是一个测试文本文件。\n包含多行内容。";
        Files.writeString(txtFile, content);

        // When
        String result = fileProcessor.extractText(txtFile.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("测试文本文件"));
    }

    @Test
    @DisplayName("提取文本 - Markdown文件")
    void testExtractText_MarkdownFile() throws IOException {
        // Given
        Path mdFile = tempDir.resolve("test.md");
        String content = "# 标题\n\n这是一个**Markdown**文件。";
        Files.writeString(mdFile, content);

        // When
        String result = fileProcessor.extractText(mdFile.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("标题"));
        assertTrue(result.contains("Markdown"));
    }

    @Test
    @DisplayName("提取文本 - 不支持的文件类型")
    void testExtractText_UnsupportedFileType() {
        // Given
        Path file = tempDir.resolve("test.exe");

        // When & Then
        assertThrows(IOException.class, () -> {
            fileProcessor.extractText(file.toFile());
        });
    }

    @Test
    @DisplayName("清理文本 - 去除多余空白")
    void testCleanText_RemoveExtraWhitespace() {
        // Given
        String text = "这是  一个   有多余    空白的   文本";

        // When
        String result = fileProcessor.cleanText(text);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("  "));  // 不应该有多个连续空格
    }

    @Test
    @DisplayName("清理文本 - 空文本")
    void testCleanText_EmptyText() {
        // When
        String result1 = fileProcessor.cleanText("");
        String result2 = fileProcessor.cleanText(null);

        // Then
        assertEquals("", result1);
        assertEquals("", result2);
    }

    @Test
    @DisplayName("清理文本 - 去除特殊字符")
    void testCleanText_RemoveSpecialCharacters() {
        // Given
        String text = "正常文本\u0000\u0001\u0002控制字符";

        // When
        String result = fileProcessor.cleanText(text);

        // Then
        assertNotNull(result);
        // 控制字符应该被移除
        assertFalse(result.contains("\u0000"));
    }

    @Test
    @DisplayName("提取文本 - 处理空文件")
    void testExtractText_EmptyFile() throws IOException {
        // Given
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.writeString(emptyFile, "");

        // When
        String result = fileProcessor.extractText(emptyFile.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty() || result.isBlank());
    }

    @Test
    @DisplayName("提取文本 - 不存在的文件")
    void testExtractText_NonExistentFile() {
        // Given
        File nonExistentFile = new File(tempDir.toFile(), "nonexistent.txt");

        // When & Then
        assertThrows(IOException.class, () -> {
            fileProcessor.extractText(nonExistentFile);
        });
    }

    @Test
    @DisplayName("提取文本 - 包含中文")
    void testExtractText_ChineseContent() throws IOException {
        // Given
        Path file = tempDir.resolve("chinese.txt");
        String content = "这是中文内容。包含标点符号！";
        Files.writeString(file, content);

        // When
        String result = fileProcessor.extractText(file.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("中文内容"));
        assertTrue(result.contains("标点符号"));
    }

    @Test
    @DisplayName("清理文本 - Trim功能")
    void testCleanText_Trim() {
        // Given
        String text = "  前后有空格  ";

        // When
        String result = fileProcessor.cleanText(text);

        // Then
        assertEquals("前后有空格", result);
    }
}

