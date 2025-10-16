package com.xu.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xu.news.common.PageResult;
import com.xu.news.entity.KnowledgeEntry;
import com.xu.news.service.KnowledgeEntryService;
import com.xu.news.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KnowledgeController API测试
 * 
 * @author XU
 * @since 2025-10-16
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("知识库控制器API测试")
class KnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KnowledgeEntryService knowledgeEntryService;

    private KnowledgeEntry testEntry;
    private List<KnowledgeEntry> testEntries;

    @BeforeEach
    void setUp() {
        testEntry = TestDataBuilder.createKnowledgeEntry();
        testEntries = Arrays.asList(
            TestDataBuilder.createKnowledgeEntry(),
            TestDataBuilder.createKnowledgeEntry(),
            TestDataBuilder.createKnowledgeEntry()
        );
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("GET /knowledge/list - 查询知识库列表")
    void testListKnowledge_Success() throws Exception {
        // Given
        PageResult<KnowledgeEntry> pageResult = new PageResult<>();
        pageResult.setList(testEntries);
        pageResult.setTotal(3L);
        pageResult.setPage(1);
        pageResult.setSize(10);
        
        when(knowledgeEntryService.list(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/knowledge/list")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list").isArray());

        verify(knowledgeEntryService, times(1)).list(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("GET /knowledge/{id} - 查询知识库详情")
    void testGetKnowledge_Success() throws Exception {
        // Given
        when(knowledgeEntryService.getById(1L)).thenReturn(testEntry);

        // When & Then
        mockMvc.perform(get("/knowledge/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testEntry.getId()))
                .andExpect(jsonPath("$.data.title").value(testEntry.getTitle()));

        verify(knowledgeEntryService, times(1)).getById(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("GET /knowledge/{id} - 知识库不存在")
    void testGetKnowledge_NotFound() throws Exception {
        // Given
        when(knowledgeEntryService.getById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/knowledge/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("知识库条目不存在"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /knowledge/upload - 上传文件成功")
    void testUploadFile_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "测试内容".getBytes()
        );
        
        when(knowledgeEntryService.importFile(any(), anyList(), anyLong()))
                .thenReturn(testEntry);

        // When & Then
        mockMvc.perform(multipart("/knowledge/upload")
                .file(file)
                .param("tags", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("上传成功"));

        verify(knowledgeEntryService, times(1)).importFile(any(), anyList(), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("POST /knowledge/upload - 文件为空")
    void testUploadFile_EmptyFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/knowledge/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("文件不能为空"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("DELETE /knowledge/{id} - 删除成功")
    void testDeleteKnowledge_Success() throws Exception {
        // Given
        when(knowledgeEntryService.removeById(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/knowledge/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(knowledgeEntryService, times(1)).removeById(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("DELETE /knowledge/batch - 批量删除")
    void testBatchDelete_Success() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(knowledgeEntryService.removeByIds(anyList())).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/knowledge/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量删除成功"));

        verify(knowledgeEntryService, times(1)).removeByIds(anyList());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("GET /knowledge/search - 搜索知识库")
    void testSearchKnowledge_Success() throws Exception {
        // Given
        when(knowledgeEntryService.search(anyString(), anyInt()))
                .thenReturn(testEntries);

        // When & Then
        mockMvc.perform(get("/knowledge/search")
                .param("keyword", "AI")
                .param("topK", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(knowledgeEntryService, times(1)).search(anyString(), anyInt());
    }

    @Test
    @DisplayName("GET /knowledge/list - 未授权访问")
    void testListKnowledge_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/knowledge/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("PUT /knowledge/{id} - 更新知识库条目")
    void testUpdateKnowledge_Success() throws Exception {
        // Given
        when(knowledgeEntryService.updateById(any(KnowledgeEntry.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/knowledge/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEntry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"));

        verify(knowledgeEntryService, times(1)).updateById(any(KnowledgeEntry.class));
    }
}

