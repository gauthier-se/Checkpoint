package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.config.SecurityConfig;
import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit testing controller logic
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    private UUID gameId;
    private ReviewResponseDto reviewResponseDto;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        reviewResponseDto = new ReviewResponseDto(UUID.randomUUID(), 5, "Great game!", false, null, null, null);
    }

    @Test
    @DisplayName("GET /api/games/{gameId}/reviews should return 200 OK")
    void shouldReturnReviews() throws Exception {
        // Given
        when(reviewService.getGameReviews(eq(gameId), any()))
                .thenReturn(new PageImpl<>(List.of(reviewResponseDto)));

        // When & Then
        mockMvc.perform(get("/api/games/{gameId}/reviews", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].score").value(5))
                .andExpect(jsonPath("$.content[0].content").value("Great game!"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/games/{gameId}/reviews should return 201 Created")
    void shouldSubmitReview() throws Exception {
        // Given
        ReviewRequestDto request = new ReviewRequestDto(5, "Great game!", false);
        when(reviewService.addOrUpdateReview(any(), eq(gameId), any())).thenReturn(reviewResponseDto);

        // When & Then
        mockMvc.perform(post("/api/games/{gameId}/reviews", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(5))
                .andExpect(jsonPath("$.content").value("Great game!"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/games/{gameId}/reviews should return 204 No Content")
    void shouldDeleteReview() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/games/{gameId}/reviews", gameId))
                .andExpect(status().isNoContent());
    }
}
