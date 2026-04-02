package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.dto.catalog.ReviewUserDto;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.ReviewService;

/**
 * Unit tests for {@link ReviewController}.
 */
@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        reviewResponseDto = new ReviewResponseDto(
                UUID.randomUUID(),
                "Great game!",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ReviewUserDto(UUID.randomUUID(), "testuser", null),
                UUID.randomUUID(),
                "PC",
                PlayStatus.COMPLETED,
                false,
                0,
                false,
                0
        );
    }

    @Nested
    @DisplayName("GET /api/games/{gameId}/reviews")
    class GetReviews {

        @Test
        @DisplayName("should return 200 OK with paginated reviews")
        void getReviews_shouldReturnPaginatedReviews() throws Exception {
            // Given
            when(reviewService.getGameReviews(eq(gameId), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(reviewResponseDto)));

            // When / Then
            mockMvc.perform(get("/api/games/{gameId}/reviews", gameId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].content").value("Great game!"))
                    .andExpect(jsonPath("$.content[0].haveSpoilers").value(false))
                    .andExpect(jsonPath("$.content[0].platformName").value("PC"))
                    .andExpect(jsonPath("$.content[0].playStatus").value("COMPLETED"));
        }

        @Test
        @DisplayName("should return 200 OK with empty page when no reviews exist")
        void getReviews_shouldReturnEmptyPage() throws Exception {
            // Given
            when(reviewService.getGameReviews(eq(gameId), any(), any()))
                    .thenReturn(new PageImpl<>(List.of()));

            // When / Then
            mockMvc.perform(get("/api/games/{gameId}/reviews", gameId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("should return 404 when game does not exist")
        void getReviews_shouldReturn404WhenGameNotFound() throws Exception {
            // Given
            when(reviewService.getGameReviews(eq(gameId), any(), any()))
                    .thenThrow(new GameNotFoundException(gameId));

            // When / Then
            mockMvc.perform(get("/api/games/{gameId}/reviews", gameId))
                    .andExpect(status().isNotFound());
        }
    }
}
