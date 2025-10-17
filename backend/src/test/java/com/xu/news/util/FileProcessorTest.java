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
 * FileProcessor 单元测试
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
    @DisplayName("处理文本文件 - TXT")
    void testProcessTextFile() throws IOException {
        // Given
        Path txtFile = tempDir.resolve("test.txt");
        String content = "这是一个测试文本文件。\n包含多行内容。";
        Files.writeString(txtFile, content);

        // When
        String result = fileProcessor.processFile(txtFile.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("测试文本文件"));
    }

    @Test
    @DisplayName("处理Markdown文件 - MD")
    void testProcessMarkdownFile() throws IOException {
        // Given
        Path mdFile = tempDir.resolve("test.md");
        String content = "# 标题\n\n这是一个**Markdown**文件。\n\n- 列表项1\n- 列表项2";
        Files.writeString(mdFile, content);

        // When
        String result = fileProcessor.processFile(mdFile.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("标题"));
        assertTrue(result.contains("Markdown"));
    }

    @Test
    @DisplayName("处理HTML文件")
    void testProcessHtmlFile() throws IOException {
        // Given
        Path htmlFile = tempDir.resolve("test.html");
        String content = "<html><body><h1>标题</h1><p>这是HTML内容</p></body></html>";
        Files.writeString(htmlFile, content);

        // When
        String result = fileProcessor.processFile(htmlFile.toFile());

        // Then
        assertNotNull(result);
        // HTML处理应该提取文本内容
        assertTrue(result.length() > 0);
    }

    @Test
    @DisplayName("处理空文件")
    void testProcessEmptyFile() throws IOException {
        // Given
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.writeString(emptyFile, "");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            fileProcessor.processFile(emptyFile.toFile());
        });
    }

    @Test
    @DisplayName("处理不存在的文件")
    void testProcessNonExistentFile() {
        // Given
        File nonExistentFile = new File(tempDir.toFile(), "nonexistent.txt");

        // When & Then
        assertThrows(IOException.class, () -> {
            fileProcessor.processFile(nonExistentFile);
        });
    }

    @Test
    @DisplayName("获取文件扩展名 - 有扩展名")
    void testGetFileExtension_WithExtension() {
        // When
        String ext1 = fileProcessor.getFileExtension("test.txt");
        String ext2 = fileProcessor.getFileExtension("document.pdf");
        String ext3 = fileProcessor.getFileExtension("image.jpg");

        // Then
        assertEquals("txt", ext1);
        assertEquals("pdf", ext2);
        assertEquals("jpg", ext3);
    }

    @Test
    @DisplayName("获取文件扩展名 - 无扩展名")
    void testGetFileExtension_WithoutExtension() {
        // When
        String ext = fileProcessor.getFileExtension("filename");

        // Then
        assertEquals("", ext);
    }

    @Test
    @DisplayName("获取文件扩展名 - 点号在开头")
    void testGetFileExtension_DotAtBeginning() {
        // When
        String ext = fileProcessor.getFileExtension(".gitignore");

        // Then
        assertEquals("", ext);
    }

    @Test
    @DisplayName("检查支持的文件类型")
    void testIsSupportedFileType() {
        // Then
        assertTrue(fileProcessor.isSupportedFileType("test.txt"));
        assertTrue(fileProcessor.isSupportedFileType("test.md"));
        assertTrue(fileProcessor.isSupportedFileType("test.html"));
        assertTrue(fileProcessor.isSupportedFileType("test.pdf"));
        
        assertFalse(fileProcessor.isSupportedFileType("test.exe"));
        assertFalse(fileProcessor.isSupportedFileType("test.bin"));
    }

    @Test
    @DisplayName("提取文本 - 处理特殊字符")
    void testExtractText_SpecialCharacters() throws IOException {
        // Given
        Path file = tempDir.resolve("special.txt");
        String content = "包含特殊字符：@#$%^&*()，中文标点：""''。";
        Files.writeString(file, content);

        // When
        String result = fileProcessor.processFile(file.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("特殊字符"));
    }

    @Test
    @DisplayName("处理大文件 - 性能测试")
    void testProcessLargeFile() throws IOException {
        // Given
        Path largeFile = tempDir.resolve("large.txt");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("这是第").append(i).append("行内容。\n");
        }
        Files.writeString(largeFile, sb.toString());

        // When
        long startTime = System.currentTimeMillis();
        String result = fileProcessor.processFile(largeFile.toFile());
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertTrue(result.length() > 0);
        // 处理时间应该在合理范围内（< 5秒）
        assertTrue(endTime - startTime < 5000);
    }

    @Test
    @DisplayName("处理不同编码的文件")
    void testProcessFileWithDifferentEncoding() throws IOException {
        // Given
        Path utf8File = tempDir.resolve("utf8.txt");
        String content = "UTF-8编码：你好世界！Hello World!";
        Files.writeString(utf8File, content);

        // When
        String result = fileProcessor.processFile(utf8File.toFile());

        // Then
        assertNotNull(result);
        assertTrue(result.contains("你好世界"));
        assertTrue(result.contains("Hello World"));
    }
}

