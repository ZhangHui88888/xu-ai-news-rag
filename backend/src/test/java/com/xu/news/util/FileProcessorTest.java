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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileProcessor 单元测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest(classes = com.xu.news.XuNewsApplication.class)
@ActiveProfiles("test")
@DisplayName("文件处理器测试")
class FileProcessorTest {

    @Autowired
    private FileProcessor fileProcessor;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("提取TXT文件文本 - 成功")
    void testExtractText_TxtFile() throws IOException {
        // Given
        String content = "这是测试文本内容\n包含多行\n用于测试";
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, content);

        // When
        String extracted = fileProcessor.extractText(testFile.toFile());

        // Then
        assertNotNull(extracted);
        assertTrue(extracted.contains("测试文本"));
    }

    @Test
    @DisplayName("提取文本 - 不支持的文件类型")
    void testExtractText_UnsupportedType() {
        // Given
        Path testFile = tempDir.resolve("test.exe");

        // When & Then
        assertThrows(IOException.class, () -> {
            fileProcessor.extractText(testFile.toFile());
        });
    }

    @Test
    @DisplayName("清理文本 - 去除多余空白")
    void testCleanText_RemoveExtraSpaces() {
        // Given
        String dirtyText = "这是   测试\n\n文本    内容";

        // When
        String cleaned = fileProcessor.cleanText(dirtyText);

        // Then
        assertNotNull(cleaned);
        assertFalse(cleaned.contains("   ")); // 没有多个连续空格
        assertEquals("这是 测试 文本 内容", cleaned);
    }

    @Test
    @DisplayName("清理文本 - 空文本")
    void testCleanText_EmptyText() {
        // When
        String cleaned = fileProcessor.cleanText("");

        // Then
        assertEquals("", cleaned);
    }

    @Test
    @DisplayName("清理文本 - Null文本")
    void testCleanText_NullText() {
        // When
        String cleaned = fileProcessor.cleanText(null);

        // Then
        assertEquals("", cleaned);
    }

    @Test
    @DisplayName("分割文本为Chunks - 成功")
    void testSplitTextIntoChunks_Success() {
        // Given
        String text = "这是一段很长的文本，需要分割成多个小块。".repeat(100);
        int maxChunkSize = 200;
        int overlap = 50;

        // When
        List<String> chunks = fileProcessor.splitTextIntoChunks(text, maxChunkSize, overlap);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.size() > 1);
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= maxChunkSize);
        }
    }

    @Test
    @DisplayName("分割文本 - 空文本")
    void testSplitTextIntoChunks_EmptyText() {
        // When
        List<String> chunks = fileProcessor.splitTextIntoChunks("", 100, 20);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.isEmpty());
    }

    @Test
    @DisplayName("验证文件大小 - 允许的大小")
    void testValidateFileSize_AllowedSize() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "小文件");
        long maxSize = 10 * 1024 * 1024; // 10MB

        // When
        boolean isValid = fileProcessor.validateFileSize(testFile.toFile(), maxSize);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证文件扩展名 - 允许的类型")
    void testValidateFileExtension_AllowedType() {
        // Given
        String filename = "document.pdf";
        List<String> allowedExtensions = Arrays.asList("pdf", "docx", "txt");

        // When
        boolean isValid = fileProcessor.validateFileExtension(filename, allowedExtensions);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证文件扩展名 - 不允许的类型")
    void testValidateFileExtension_DisallowedType() {
        // Given
        String filename = "virus.exe";
        List<String> allowedExtensions = Arrays.asList("pdf", "docx", "txt");

        // When
        boolean isValid = fileProcessor.validateFileExtension(filename, allowedExtensions);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("获取文件扩展名 - 成功")
    void testGetFileExtension_Success() {
        // When
        String ext1 = fileProcessor.getFileExtension("document.pdf");
        String ext2 = fileProcessor.getFileExtension("test.txt");

        // Then
        assertEquals("pdf", ext1);
        assertEquals("txt", ext2);
    }

    @Test
    @DisplayName("获取文件扩展名 - 无扩展名")
    void testGetFileExtension_NoExtension() {
        // When
        String ext = fileProcessor.getFileExtension("README");

        // Then
        assertEquals("", ext);
    }
}

