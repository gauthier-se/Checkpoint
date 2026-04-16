package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.admin.AdminReportDetailDto;
import com.checkpoint.api.dto.admin.AdminReportDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.exceptions.ReportNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.AdminReportService;

@WebMvcTest(AdminReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminReportService adminReportService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/admin/reports should return paginated reports")
    void getAllReports_shouldReturnPagedReports() throws Exception {
        // Given
        UUID id1 = UUID.randomUUID();
        AdminReportDto dto1 = new AdminReportDto(id1, "reporter1", "Spam content", "review", "Some offensive...", LocalDateTime.now());
        PagedResponseDto.PageMetadata meta = new PagedResponseDto.PageMetadata(0, 20, 1, 1, true, true, false, false);
        PagedResponseDto<AdminReportDto> response = new PagedResponseDto<>(List.of(dto1), meta);

        when(adminReportService.getAllReports(any(Pageable.class), isNull())).thenReturn(response);

        // When / Then
        mockMvc.perform(get("/api/admin/reports")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.content[0].reporterUsername").value("reporter1"))
                .andExpect(jsonPath("$.content[0].reason").value("Spam content"))
                .andExpect(jsonPath("$.content[0].type").value("review"));

        verify(adminReportService).getAllReports(any(Pageable.class), isNull());
    }

    @Test
    @DisplayName("GET /api/admin/reports with type filter should return filtered reports")
    void getAllReports_withTypeFilter_shouldReturnFilteredReports() throws Exception {
        // Given
        UUID id1 = UUID.randomUUID();
        AdminReportDto dto1 = new AdminReportDto(id1, "reporter1", "Bad comment", "comment", "Offensive comment...", LocalDateTime.now());
        PagedResponseDto.PageMetadata meta = new PagedResponseDto.PageMetadata(0, 20, 1, 1, true, true, false, false);
        PagedResponseDto<AdminReportDto> response = new PagedResponseDto<>(List.of(dto1), meta);

        when(adminReportService.getAllReports(any(Pageable.class), eq("comment"))).thenReturn(response);

        // When / Then
        mockMvc.perform(get("/api/admin/reports")
                        .param("page", "0")
                        .param("size", "20")
                        .param("type", "comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("comment"));

        verify(adminReportService).getAllReports(any(Pageable.class), eq("comment"));
    }

    @Test
    @DisplayName("GET /api/admin/reports/{id} should return report detail")
    void getReportById_shouldReturnReportDetail() throws Exception {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AdminReportDetailDto detail = new AdminReportDetailDto(
                reportId, "reporter1", "Spam", "review", targetId, "author1", "Full offensive content here", LocalDateTime.now());

        when(adminReportService.getReportById(reportId)).thenReturn(detail);

        // When / Then
        mockMvc.perform(get("/api/admin/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.reporterUsername").value("reporter1"))
                .andExpect(jsonPath("$.reason").value("Spam"))
                .andExpect(jsonPath("$.type").value("review"))
                .andExpect(jsonPath("$.targetId").value(targetId.toString()))
                .andExpect(jsonPath("$.targetAuthorUsername").value("author1"))
                .andExpect(jsonPath("$.targetFullContent").value("Full offensive content here"));

        verify(adminReportService).getReportById(reportId);
    }

    @Test
    @DisplayName("GET /api/admin/reports/{id} should return 404 when not found")
    void getReportById_shouldReturn404WhenNotFound() throws Exception {
        // Given
        UUID reportId = UUID.randomUUID();
        when(adminReportService.getReportById(reportId)).thenThrow(new ReportNotFoundException(reportId));

        // When / Then
        mockMvc.perform(get("/api/admin/reports/{id}", reportId))
                .andExpect(status().isNotFound());

        verify(adminReportService).getReportById(reportId);
    }

    @Test
    @DisplayName("DELETE /api/admin/reports/{id} should return 204 No Content")
    void dismissReport_shouldReturn204() throws Exception {
        // Given
        UUID reportId = UUID.randomUUID();
        doNothing().when(adminReportService).dismissReport(reportId);

        // When / Then
        mockMvc.perform(delete("/api/admin/reports/{id}", reportId))
                .andExpect(status().isNoContent());

        verify(adminReportService).dismissReport(reportId);
    }

    @Test
    @DisplayName("DELETE /api/admin/reports/{id} should return 404 when not found")
    void dismissReport_shouldReturn404WhenNotFound() throws Exception {
        // Given
        UUID reportId = UUID.randomUUID();
        doThrow(new ReportNotFoundException(reportId)).when(adminReportService).dismissReport(reportId);

        // When / Then
        mockMvc.perform(delete("/api/admin/reports/{id}", reportId))
                .andExpect(status().isNotFound());

        verify(adminReportService).dismissReport(reportId);
    }
}
