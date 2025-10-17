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
     * 从输入流提取文本内容（不需要保存文件）
     */
    public String extractText(java.io.InputStream inputStream, String filename) throws IOException {
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".pdf")) {
            return extractPdfTextFromStream(inputStream);
        } else if (lowerFilename.endsWith(".docx")) {
            return extractDocxTextFromStream(inputStream);
        } else if (lowerFilename.endsWith(".txt") || lowerFilename.endsWith(".md")) {
            return extractPlainTextFromStream(inputStream);
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
     * 从输入流提取PDF文本
     */
    private String extractPdfTextFromStream(java.io.InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 从输入流提取DOCX文本
     */
    private String extractDocxTextFromStream(java.io.InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }
            
            return text.toString();
        }
    }

    /**
     * 从输入流提取纯文本
     */
    private String extractPlainTextFromStream(java.io.InputStream inputStream) throws IOException {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
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
        java.util.List<String> chunks = new java.util.ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        
        int start = 0;
        int length = text.length();
        
        while (start < length) {
            int end = Math.min(start + maxChunkSize, length);
            chunks.add(text.substring(start, end));
            start += (maxChunkSize - overlap);
        }
        
        return chunks;
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

