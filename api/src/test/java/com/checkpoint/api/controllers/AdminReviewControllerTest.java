package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

import com.checkpoint.api.dto.admin.AdminReportedReviewDto;
import com.checkpoint.api.dto.admin.AdminReviewDto;
import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.AdminReviewService;

@WebMvcTest(AdminReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminReviewService adminReviewService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/admin/reviews should return paginated list")
    void getAllReviews_shouldReturnPaginatedList() throws Exception {
        // Given
        UUID id1 = UUID.randomUUID();
        AdminReviewDto dto1 = new AdminReviewDto(id1, "Test review", false, "user1", "Game 1", LocalDateTime.now());
        PagedResponseDto.PageMetadata meta = new PagedResponseDto.PageMetadata(0, 20, 1, 1, true, true, false, false);
        PagedResponseDto<AdminReviewDto> response = new PagedResponseDto<>(List.of(dto1), meta);

        when(adminReviewService.getAllReviews(any(Pageable.class))).thenReturn(response);

        // When / Then
        mockMvc.perform(get("/api/admin/reviews")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.content[0].content").value("Test review"))
                .andExpect(jsonPath("$.content[0].authorUsername").value("user1"))
                .andExpect(jsonPath("$.content[0].gameTitle").value("Game 1"));

        verify(adminReviewService).getAllReviews(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/admin/reviews/reported should return paginated reported reviews")
    void getReportedReviews_shouldReturnPaginatedList() throws Exception {
        // Given
        UUID id1 = UUID.randomUUID();
        AdminReportedReviewDto dto1 = new AdminReportedReviewDto(id1, "Offensive review", "user1", "Game 1", 3, LocalDateTime.now());
        PagedResponseDto.PageMetadata meta = new PagedResponseDto.PageMetadata(0, 20, 1, 1, true, true, false, false);
        PagedResponseDto<AdminReportedReviewDto> response = new PagedResponseDto<>(List.of(dto1), meta);

        when(adminReviewService.getReportedReviews(any(Pageable.class))).thenReturn(response);

        // When / Then
        mockMvc.perform(get("/api/admin/reviews/reported")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.content[0].content").value("Offensive review"))
                .andExpect(jsonPath("$.content[0].authorUsername").value("user1"))
                .andExpect(jsonPath("$.content[0].gameTitle").value("Game 1"))
                .andExpect(jsonPath("$.content[0].reportCount").value(3));

        verify(adminReviewService).getReportedReviews(any(Pageable.class));
    }

    @Test
    @DisplayName("DELETE /api/admin/reviews/{id} should return 204 No Content")
    void deleteReview_shouldReturn204() throws Exception {
        // Given
        UUID reviewId = UUID.randomUUID();
        doNothing().when(adminReviewService).deleteReview(reviewId);

        // When / Then
        mockMvc.perform(delete("/api/admin/reviews/{id}", reviewId))
                .andExpect(status().isNoContent());

        verify(adminReviewService).deleteReview(reviewId);
    }
}
