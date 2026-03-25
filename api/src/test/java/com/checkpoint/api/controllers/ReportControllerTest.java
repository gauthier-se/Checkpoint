package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.catalog.ReportRequestDto;
import com.checkpoint.api.dto.catalog.ReportResponseDto;
import com.checkpoint.api.exceptions.DuplicateReportException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link ReportController}.
 */
@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    private UUID reviewId;
    private ReportResponseDto reportResponseDto;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        reportResponseDto = new ReportResponseDto(
                UUID.randomUUID(),
                "Inappropriate content",
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("POST /api/reviews/{reviewId}/report")
    class ReportReview {

        @Test
        @DisplayName("should create report and return 201")
        @WithMockUser(username = "user@example.com")
        void reportReview_shouldReturn201() throws Exception {
            // Given
            ReportRequestDto request = new ReportRequestDto("Inappropriate content");
            when(reportService.reportReview(eq("user@example.com"), eq(reviewId), any(ReportRequestDto.class)))
                    .thenReturn(reportResponseDto);

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/report", reviewId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(reportResponseDto.id().toString()))
                    .andExpect(jsonPath("$.content").value("Inappropriate content"));
        }

        @Test
        @DisplayName("should return 404 when review not found")
        @WithMockUser(username = "user@example.com")
        void reportReview_shouldReturn404WhenReviewNotFound() throws Exception {
            // Given
            ReportRequestDto request = new ReportRequestDto("Inappropriate content");
            when(reportService.reportReview(eq("user@example.com"), eq(reviewId), any(ReportRequestDto.class)))
                    .thenThrow(new ReviewNotFoundException("Review not found with ID: " + reviewId));

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/report", reviewId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 409 when user already reported this review")
        @WithMockUser(username = "user@example.com")
        void reportReview_shouldReturn409WhenAlreadyReported() throws Exception {
            // Given
            ReportRequestDto request = new ReportRequestDto("Inappropriate content");
            when(reportService.reportReview(eq("user@example.com"), eq(reviewId), any(ReportRequestDto.class)))
                    .thenThrow(new DuplicateReportException(reviewId));

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/report", reviewId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 400 when content is blank")
        @WithMockUser(username = "user@example.com")
        void reportReview_shouldReturn400WhenContentBlank() throws Exception {
            // Given
            ReportRequestDto request = new ReportRequestDto("");

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/report", reviewId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
