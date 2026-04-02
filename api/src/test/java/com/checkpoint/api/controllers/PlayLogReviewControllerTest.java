package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.dto.catalog.ReviewUserDto;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.exceptions.PlayLogNotFoundException;
import com.checkpoint.api.exceptions.ReviewAlreadyExistsException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link PlayLogReviewController}.
 */
@WebMvcTest(controllers = PlayLogReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlayLogReviewControllerTest {

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

    private UUID playId;
    private ReviewResponseDto reviewResponseDto;

    @BeforeEach
    void setUp() {
        playId = UUID.randomUUID();
        reviewResponseDto = new ReviewResponseDto(
                UUID.randomUUID(),
                "Great game!",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ReviewUserDto(UUID.randomUUID(), "testuser", null),
                playId,
                "PC",
                PlayStatus.COMPLETED,
                false,
                0,
                false,
                0
        );
    }

    @Nested
    @DisplayName("POST /api/me/plays/{playId}/review")
    class CreateReview {

        @Test
        @DisplayName("should create review and return 201")
        @WithMockUser(username = "user@example.com")
        void createReview_shouldReturn201() throws Exception {
            // Given
            ReviewRequestDto request = new ReviewRequestDto("Great game!", false);
            when(reviewService.createPlayLogReview(eq("user@example.com"), eq(playId), any(ReviewRequestDto.class)))
                    .thenReturn(reviewResponseDto);

            // When / Then
            mockMvc.perform(post("/api/me/plays/{playId}/review", playId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("Great game!"))
                    .andExpect(jsonPath("$.haveSpoilers").value(false))
                    .andExpect(jsonPath("$.playLogId").value(playId.toString()))
                    .andExpect(jsonPath("$.platformName").value("PC"));
        }

        @Test
        @DisplayName("should return 409 when play log already has a review")
        @WithMockUser(username = "user@example.com")
        void createReview_shouldReturn409WhenReviewAlreadyExists() throws Exception {
            // Given
            ReviewRequestDto request = new ReviewRequestDto("Great game!", false);
            when(reviewService.createPlayLogReview(eq("user@example.com"), eq(playId), any(ReviewRequestDto.class)))
                    .thenThrow(new ReviewAlreadyExistsException(playId));

            // When / Then
            mockMvc.perform(post("/api/me/plays/{playId}/review", playId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 404 when play log not found")
        @WithMockUser(username = "user@example.com")
        void createReview_shouldReturn404WhenPlayLogNotFound() throws Exception {
            // Given
            ReviewRequestDto request = new ReviewRequestDto("Great game!", false);
            when(reviewService.createPlayLogReview(eq("user@example.com"), eq(playId), any(ReviewRequestDto.class)))
                    .thenThrow(new PlayLogNotFoundException("Play log not found with ID: " + playId));

            // When / Then
            mockMvc.perform(post("/api/me/plays/{playId}/review", playId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/me/plays/{playId}/review")
    class UpdateReview {

        @Test
        @DisplayName("should update review and return 200")
        @WithMockUser(username = "user@example.com")
        void updateReview_shouldReturn200() throws Exception {
            // Given
            ReviewRequestDto request = new ReviewRequestDto("Updated review", true);
            ReviewResponseDto updatedResponse = new ReviewResponseDto(
                    reviewResponseDto.id(),
                    "Updated review",
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    new ReviewUserDto(UUID.randomUUID(), "testuser", null),
                    playId,
                    "PC",
                    PlayStatus.COMPLETED,
                    false,
                    0,
                    false,
                    0
            );
            when(reviewService.updatePlayLogReview(eq("user@example.com"), eq(playId), any(ReviewRequestDto.class)))
                    .thenReturn(updatedResponse);

            // When / Then
            mockMvc.perform(put("/api/me/plays/{playId}/review", playId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Updated review"))
                    .andExpect(jsonPath("$.haveSpoilers").value(true));
        }

        @Test
        @DisplayName("should return 404 when no review exists for play log")
        @WithMockUser(username = "user@example.com")
        void updateReview_shouldReturn404WhenReviewNotFound() throws Exception {
            // Given
            ReviewRequestDto request = new ReviewRequestDto("Updated review", true);
            when(reviewService.updatePlayLogReview(eq("user@example.com"), eq(playId), any(ReviewRequestDto.class)))
                    .thenThrow(new ReviewNotFoundException(playId));

            // When / Then
            mockMvc.perform(put("/api/me/plays/{playId}/review", playId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/me/plays/{playId}/review")
    class DeleteReview {

        @Test
        @DisplayName("should delete review and return 204")
        @WithMockUser(username = "user@example.com")
        void deleteReview_shouldReturn204() throws Exception {
            // Given
            doNothing().when(reviewService).deletePlayLogReview("user@example.com", playId);

            // When / Then
            mockMvc.perform(delete("/api/me/plays/{playId}/review", playId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when no review exists for play log")
        @WithMockUser(username = "user@example.com")
        void deleteReview_shouldReturn404WhenReviewNotFound() throws Exception {
            // Given
            doThrow(new ReviewNotFoundException(playId))
                    .when(reviewService).deletePlayLogReview("user@example.com", playId);

            // When / Then
            mockMvc.perform(delete("/api/me/plays/{playId}/review", playId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/me/plays/{playId}/review")
    class GetReview {

        @Test
        @DisplayName("should return review and return 200")
        @WithMockUser(username = "user@example.com")
        void getReview_shouldReturn200() throws Exception {
            // Given
            when(reviewService.getPlayLogReview("user@example.com", playId))
                    .thenReturn(reviewResponseDto);

            // When / Then
            mockMvc.perform(get("/api/me/plays/{playId}/review", playId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Great game!"))
                    .andExpect(jsonPath("$.playLogId").value(playId.toString()));
        }

        @Test
        @DisplayName("should return 404 when no review exists for play log")
        @WithMockUser(username = "user@example.com")
        void getReview_shouldReturn404WhenReviewNotFound() throws Exception {
            // Given
            when(reviewService.getPlayLogReview("user@example.com", playId))
                    .thenThrow(new ReviewNotFoundException(playId));

            // When / Then
            mockMvc.perform(get("/api/me/plays/{playId}/review", playId))
                    .andExpect(status().isNotFound());
        }
    }
}
