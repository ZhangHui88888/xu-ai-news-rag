package com.xu.news.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * 文件处理工具类
 * 
 * @author XU
 * @since 2025-10-15
 */
@Component
public class FileProcessor {

    /**
     * 提取文件文本内容
     */
    public String extractText(File file) throws IOException {
        String filename = file.getName().toLowerCase();
        
        if (filename.endsWith(".pdf")) {
            return extractPdfText(file);
        } else if (filename.endsWith(".docx")) {
            return extractDocxText(file);
        } else if (filename.endsWith(".txt") || filename.endsWith(".md")) {
            return extractPlainText(file);
        } else {
            throw new IOException("不支持的文件类型: " + filename);
        }
    }

    /**
     * 提取PDF文本
     */
    private String extractPdfText(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 提取DOCX文本
     */
    private String extractDocxText(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }
            
            return text.toString();
        }
    }

    /**
     * 提取纯文本
     */
    private String extractPlainText(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    /**
     * 清理文本（去除多余空白、特殊字符等）
     */
    public String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // 去除多余的空白字符
        text = text.replaceAll("\\s+", " ");
        
        // 去除特殊控制字符
        text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        return text.trim();
    }

    /**
     * 分割文本为chunks（用于向量化）
     */
    public List<String> splitTextIntoChunks(String text, int maxChunkSize, int overlap) {
        return cn.hutool.core.text.StrSplitter.split(
            text, 
            maxChunkSize, 
            true, 
            true
        );
    }

    /**
     * 验证文件大小
     */
    public boolean validateFileSize(File file, long maxSizeBytes) {
        return file.length() <= maxSizeBytes;
    }

    /**
     * 验证文件扩展名
     */
    public boolean validateFileExtension(String filename, List<String> allowedExtensions) {
        String extension = getFileExtension(filename);
        return allowedExtensions.contains(extension.toLowerCase());
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex + 1);
    }
}

