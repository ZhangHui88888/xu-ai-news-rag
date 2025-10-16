package com.xu.news.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileProcessor 单元测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("文件处理器测试")
class FileProcessorTest {

    @TempDir
    Path tempDir;

    private FileProcessor fileProcessor;

    @BeforeEach
    void setUp() {
        fileProcessor = new FileProcessor(tempDir.toString());
    }

    @Test
    @DisplayName("处理TXT文件 - 成功")
    void testProcessTextFile_Success() throws IOException {
        // Given
        String content = "这是一个测试文本文件的内容。";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content.getBytes()
        );

        // When
        String extractedContent = fileProcessor.processFile(file);

        // Then
        assertNotNull(extractedContent);
        assertTrue(extractedContent.contains("测试文本"));
    }

    @Test
    @DisplayName("处理PDF文件 - 成功")
    void testProcessPdfFile_Success() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "PDF content".getBytes()
        );

        // When & Then
        assertDoesNotThrow(() -> fileProcessor.processFile(file));
    }

    @Test
    @DisplayName("处理文件 - 空文件")
    void testProcessFile_EmptyFile() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileProcessor.processFile(file);
        });
    }

    @Test
    @DisplayName("保存文件 - 成功")
    void testSaveFile_Success() throws IOException {
        // Given
        String content = "测试内容";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content.getBytes()
        );

        // When
        String savedPath = fileProcessor.saveFile(file);

        // Then
        assertNotNull(savedPath);
        assertTrue(Files.exists(Path.of(savedPath)));
    }

    @Test
    @DisplayName("删除文件 - 成功")
    void testDeleteFile_Success() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "测试内容");

        // When
        boolean deleted = fileProcessor.deleteFile(testFile.toString());

        // Then
        assertTrue(deleted);
        assertFalse(Files.exists(testFile));
    }

    @Test
    @DisplayName("删除文件 - 文件不存在")
    void testDeleteFile_FileNotExists() {
        // Given
        String nonExistentFile = tempDir.resolve("nonexistent.txt").toString();

        // When
        boolean deleted = fileProcessor.deleteFile(nonExistentFile);

        // Then
        assertFalse(deleted);
    }

    @Test
    @DisplayName("验证文件类型 - 允许的类型")
    void testValidateFileType_AllowedType() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "content".getBytes()
        );

        // When
        boolean isValid = fileProcessor.isAllowedFileType(file);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证文件类型 - 不允许的类型")
    void testValidateFileType_DisallowedType() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.exe",
            "application/x-msdownload",
            "content".getBytes()
        );

        // When
        boolean isValid = fileProcessor.isAllowedFileType(file);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证文件大小 - 允许的大小")
    void testValidateFileSize_AllowedSize() {
        // Given
        byte[] content = new byte[1024 * 1024]; // 1MB
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content
        );

        // When
        boolean isValid = fileProcessor.isValidFileSize(file, 10 * 1024 * 1024); // 10MB限制

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证文件大小 - 超过限制")
    void testValidateFileSize_ExceedsLimit() {
        // Given
        byte[] content = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content
        );

        // When
        boolean isValid = fileProcessor.isValidFileSize(file, 10 * 1024 * 1024); // 10MB限制

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("提取文本内容 - 普通文本")
    void testExtractText_PlainText() {
        // Given
        String content = "这是测试文本\n包含多行\n内容";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content.getBytes()
        );

        // When
        String extracted = fileProcessor.extractText(file);

        // Then
        assertNotNull(extracted);
        assertTrue(extracted.contains("测试文本"));
        assertTrue(extracted.contains("多行"));
    }

    @Test
    @DisplayName("生成唯一文件名 - 成功")
    void testGenerateUniqueFilename_Success() {
        // Given
        String originalFilename = "test.txt";

        // When
        String uniqueFilename = fileProcessor.generateUniqueFilename(originalFilename);

        // Then
        assertNotNull(uniqueFilename);
        assertTrue(uniqueFilename.endsWith(".txt"));
        assertNotEquals(originalFilename, uniqueFilename);
    }
}

