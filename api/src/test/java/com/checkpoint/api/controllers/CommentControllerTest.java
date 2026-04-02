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
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.social.CommentResponseDto;
import com.checkpoint.api.dto.social.CommentUserDto;
import com.checkpoint.api.exceptions.CommentNotFoundException;
import com.checkpoint.api.exceptions.GameListNotFoundException;
import com.checkpoint.api.exceptions.ReviewNotFoundException;
import com.checkpoint.api.exceptions.UnauthorizedCommentAccessException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.CommentService;

/**
 * Unit tests for {@link CommentController}.
 */
@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    private CommentResponseDto createTestComment() {
        return new CommentResponseDto(
                UUID.randomUUID(),
                "Great review!",
                new CommentUserDto(UUID.randomUUID(), "testuser", null),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("GET /api/reviews/{reviewId}/comments")
    class GetReviewComments {

        @Test
        @DisplayName("should return paginated comments for a review")
        void getReviewComments_shouldReturnPaginatedComments() throws Exception {
            // Given
            UUID reviewId = UUID.randomUUID();
            CommentResponseDto comment = createTestComment();
            Page<CommentResponseDto> page = new PageImpl<>(List.of(comment), PageRequest.of(0, 20), 1);

            when(commentService.getReviewComments(eq(reviewId), any())).thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/reviews/{reviewId}/comments", reviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].content").value("Great review!"))
                    .andExpect(jsonPath("$.content[0].user.pseudo").value("testuser"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/reviews/{reviewId}/comments")
    class AddReviewComment {

        @Test
        @DisplayName("should create comment and return 201")
        @WithMockUser(username = "user@example.com")
        void addReviewComment_shouldCreateComment() throws Exception {
            // Given
            UUID reviewId = UUID.randomUUID();
            CommentResponseDto response = createTestComment();

            when(commentService.addReviewComment(eq("user@example.com"), eq(reviewId), eq("Great review!")))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/comments", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\": \"Great review!\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("Great review!"));
        }

        @Test
        @DisplayName("should return 404 when review not found")
        @WithMockUser(username = "user@example.com")
        void addReviewComment_shouldReturn404WhenReviewNotFound() throws Exception {
            // Given
            UUID reviewId = UUID.randomUUID();

            when(commentService.addReviewComment(eq("user@example.com"), eq(reviewId), eq("Nice!")))
                    .thenThrow(new ReviewNotFoundException("Review not found with ID: " + reviewId));

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/comments", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\": \"Nice!\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when content is blank")
        @WithMockUser(username = "user@example.com")
        void addReviewComment_shouldReturn400WhenContentBlank() throws Exception {
            // Given
            UUID reviewId = UUID.randomUUID();

            // When / Then
            mockMvc.perform(post("/api/reviews/{reviewId}/comments", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\": \"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/lists/{listId}/comments")
    class GetListComments {

        @Test
        @DisplayName("should return paginated comments for a list")
        void getListComments_shouldReturnPaginatedComments() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            CommentResponseDto comment = createTestComment();
            Page<CommentResponseDto> page = new PageImpl<>(List.of(comment), PageRequest.of(0, 20), 1);

            when(commentService.getListComments(eq(listId), any())).thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/lists/{listId}/comments", listId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].content").value("Great review!"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/lists/{listId}/comments")
    class AddListComment {

        @Test
        @DisplayName("should create comment and return 201")
        @WithMockUser(username = "user@example.com")
        void addListComment_shouldCreateComment() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            CommentResponseDto response = createTestComment();

            when(commentService.addListComment(eq("user@example.com"), eq(listId), eq("Nice list!")))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/lists/{listId}/comments", listId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\": \"Nice list!\"}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should return 404 when list not found")
        @WithMockUser(username = "user@example.com")
        void addListComment_shouldReturn404WhenListNotFound() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();

            when(commentService.addListComment(eq("user@example.com"), eq(listId), eq("Hello")))
                    .thenThrow(new GameListNotFoundException(listId));

            // When / Then
            mockMvc.perform(post("/api/lists/{listId}/comments", listId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\": \"Hello\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/comments/{commentId}")
    class UpdateComment {

        @Test
        @DisplayName("should update comment and return 200")
        @WithMockUser(username = "user@example.com")
        void updateComment_shouldUpdateComment() throws Exception {
            // Given
            UUID commentId = UUID.randomUUID();
            CommentResponseDto response = new CommentResponseDto(
                    commentId, "Updated content",
                    new CommentUserDto(UUID.randomUUID(), "testuser", null),
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(commentService.updateComment(eq("user@example.com"), eq(commentId), eq("Updated content")))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(put("/api/comments/{commentId}", commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\": \"Updated content\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Updated content"));
        }

        @Test
        @DisplayName("should return 404 when comment not found")
        @WithMockUser(username = "user@example.com")
        void updateComment_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID commentId = UUID.randomUUID();

            when(commentService.updateComment(eq("user@example.com"), eq(commentId), eq("Updated")))
                    .thenThrow(new CommentNotFoundException(commentId));

            // When / Then
            mockMvc.perform(put("/api/comments/{commentId}", commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\": \"Updated\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 when not comment owner")
        @WithMockUser(username = "user@example.com")
        void updateComment_shouldReturn403WhenNotOwner() throws Exception {
            // Given
            UUID commentId = UUID.randomUUID();

            when(commentService.updateComment(eq("user@example.com"), eq(commentId), eq("Hacked")))
                    .thenThrow(new UnauthorizedCommentAccessException(commentId));

            // When / Then
            mockMvc.perform(put("/api/comments/{commentId}", commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\": \"Hacked\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/comments/{commentId}")
    class DeleteComment {

        @Test
        @DisplayName("should delete comment and return 204")
        @WithMockUser(username = "user@example.com")
        void deleteComment_shouldDeleteComment() throws Exception {
            // Given
            UUID commentId = UUID.randomUUID();

            doNothing().when(commentService).deleteComment("user@example.com", commentId);

            // When / Then
            mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when comment not found")
        @WithMockUser(username = "user@example.com")
        void deleteComment_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID commentId = UUID.randomUUID();

            doThrow(new CommentNotFoundException(commentId))
                    .when(commentService).deleteComment("user@example.com", commentId);

            // When / Then
            mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 when not comment owner")
        @WithMockUser(username = "user@example.com")
        void deleteComment_shouldReturn403WhenNotOwner() throws Exception {
            // Given
            UUID commentId = UUID.randomUUID();

            doThrow(new UnauthorizedCommentAccessException(commentId))
                    .when(commentService).deleteComment("user@example.com", commentId);

            // When / Then
            mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                    .andExpect(status().isForbidden());
        }
    }
}
