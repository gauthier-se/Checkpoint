package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.social.LikeResponseDto;
import com.checkpoint.api.exceptions.GameListNotFoundException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.LikeService;

/**
 * Unit tests for {@link LikeController}.
 */
@WebMvcTest(LikeController.class)
@AutoConfigureMockMvc(addFilters = false)
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikeService likeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("POST /api/reviews/{reviewId}/like")
    class ToggleReviewLike {

        @Test
        @DisplayName("should like review and return 200")
        @WithMockUser(username = "user@example.com")
        void toggleReviewLike_shouldLikeReview() throws Exception {
            // Given
            UUID reviewId = UUID.randomUUID();
            LikeResponseDto response = new LikeResponseDto(true, 5);

            when(likeService.toggleReviewLike(eq("user@example.com"), eq(reviewId)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.liked").value(true))
                    .andExpect(jsonPath("$.likesCount").value(5));
        }

        @Test
        @DisplayName("should unlike review and return 200")
        @WithMockUser(username = "user@example.com")
        void toggleReviewLike_shouldUnlikeReview() throws Exception {
            // Given
            UUID reviewId = UUID.randomUUID();
            LikeResponseDto response = new LikeResponseDto(false, 4);

            when(likeService.toggleReviewLike(eq("user@example.com"), eq(reviewId)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.liked").value(false))
                    .andExpect(jsonPath("$.likesCount").value(4));
        }

        @Test
        @DisplayName("should return 404 when review not found")
        @WithMockUser(username = "user@example.com")
        void toggleReviewLike_shouldReturn404WhenReviewNotFound() throws Exception {
            // Given
            UUID reviewId = UUID.randomUUID();

            when(likeService.toggleReviewLike(eq("user@example.com"), eq(reviewId)))
                    .thenThrow(new ReviewNotFoundException("Review not found with ID: " + reviewId));

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("POST /api/lists/{listId}/like")
    class ToggleListLike {

        @Test
        @DisplayName("should like list and return 200")
        @WithMockUser(username = "user@example.com")
        void toggleListLike_shouldLikeList() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            LikeResponseDto response = new LikeResponseDto(true, 10);

            when(likeService.toggleListLike(eq("user@example.com"), eq(listId)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/lists/{listId}/like", listId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.liked").value(true))
                    .andExpect(jsonPath("$.likesCount").value(10));
        }

        @Test
        @DisplayName("should unlike list and return 200")
        @WithMockUser(username = "user@example.com")
        void toggleListLike_shouldUnlikeList() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            LikeResponseDto response = new LikeResponseDto(false, 9);

            when(likeService.toggleListLike(eq("user@example.com"), eq(listId)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/lists/{listId}/like", listId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.liked").value(false))
                    .andExpect(jsonPath("$.likesCount").value(9));
        }

        @Test
        @DisplayName("should return 404 when list not found")
        @WithMockUser(username = "user@example.com")
        void toggleListLike_shouldReturn404WhenListNotFound() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();

            when(likeService.toggleListLike(eq("user@example.com"), eq(listId)))
                    .thenThrow(new GameListNotFoundException(listId));

            // When / Then
            mockMvc.perform(post("/api/lists/{listId}/like", listId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}
