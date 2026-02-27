package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.collection.BacklogResponseDto;
import com.checkpoint.api.exceptions.GameAlreadyInBacklogException;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.GameNotInBacklogException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.BacklogService;

/**
 * Unit tests for {@link BacklogController}.
 */
@WebMvcTest(BacklogController.class)
@AutoConfigureMockMvc(addFilters = false)
class BacklogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BacklogService backlogService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("POST /api/me/backlog/{videoGameId}")
    class AddToBacklog {

        @Test
        @DisplayName("should add game to backlog and return 201")
        @WithMockUser(username = "user@example.com")
        void addToBacklog_shouldReturn201() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID backlogId = UUID.randomUUID();
            BacklogResponseDto response = new BacklogResponseDto(
                    backlogId, videoGameId, "The Witcher 3", "cover.jpg",
                    LocalDate.of(2015, 5, 19), LocalDateTime.now());

            when(backlogService.addToBacklog(eq("user@example.com"), eq(videoGameId)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/me/backlog/{videoGameId}", videoGameId))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(backlogId.toString()))
                    .andExpect(jsonPath("$.videoGameId").value(videoGameId.toString()))
                    .andExpect(jsonPath("$.title").value("The Witcher 3"));
        }

        @Test
        @DisplayName("should return 409 when game already in backlog")
        @WithMockUser(username = "user@example.com")
        void addToBacklog_shouldReturn409WhenAlreadyInBacklog() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();

            when(backlogService.addToBacklog(eq("user@example.com"), eq(videoGameId)))
                    .thenThrow(new GameAlreadyInBacklogException(videoGameId));

            // When / Then
            mockMvc.perform(post("/api/me/backlog/{videoGameId}", videoGameId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"));
        }

        @Test
        @DisplayName("should return 404 when video game not found")
        @WithMockUser(username = "user@example.com")
        void addToBacklog_shouldReturn404WhenGameNotFound() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();

            when(backlogService.addToBacklog(eq("user@example.com"), eq(videoGameId)))
                    .thenThrow(new GameNotFoundException(videoGameId));

            // When / Then
            mockMvc.perform(post("/api/me/backlog/{videoGameId}", videoGameId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("DELETE /api/me/backlog/{videoGameId}")
    class RemoveFromBacklog {

        @Test
        @DisplayName("should remove game and return 204")
        @WithMockUser(username = "user@example.com")
        void removeFromBacklog_shouldReturn204() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            doNothing().when(backlogService)
                    .removeFromBacklog("user@example.com", videoGameId);

            // When / Then
            mockMvc.perform(delete("/api/me/backlog/{videoGameId}", videoGameId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when game not in backlog")
        @WithMockUser(username = "user@example.com")
        void removeFromBacklog_shouldReturn404WhenNotInBacklog() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            doThrow(new GameNotInBacklogException(videoGameId))
                    .when(backlogService)
                    .removeFromBacklog("user@example.com", videoGameId);

            // When / Then
            mockMvc.perform(delete("/api/me/backlog/{videoGameId}", videoGameId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/me/backlog")
    class GetUserBacklog {

        @Test
        @DisplayName("should return paginated backlog")
        @WithMockUser(username = "user@example.com")
        void getBacklog_shouldReturnPaginatedBacklog() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID backlogId = UUID.randomUUID();
            List<BacklogResponseDto> items = List.of(
                    new BacklogResponseDto(backlogId, videoGameId, "Elden Ring", "cover.jpg",
                            LocalDate.of(2022, 2, 25), LocalDateTime.now())
            );
            Page<BacklogResponseDto> page = new PageImpl<>(items);

            when(backlogService.getUserBacklog(eq("user@example.com"), any(Pageable.class)))
                    .thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/me/backlog"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("Elden Ring"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }

        @Test
        @DisplayName("should accept pagination parameters")
        @WithMockUser(username = "user@example.com")
        void getBacklog_shouldAcceptPaginationParams() throws Exception {
            // Given
            Page<BacklogResponseDto> emptyPage = new PageImpl<>(List.of());
            when(backlogService.getUserBacklog(eq("user@example.com"), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // When / Then
            mockMvc.perform(get("/api/me/backlog")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sort", "createdAt,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/me/backlog/{videoGameId}/status")
    class IsInBacklog {

        @Test
        @DisplayName("should return true when game is in backlog")
        @WithMockUser(username = "user@example.com")
        void isInBacklog_shouldReturnTrue() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            when(backlogService.isInBacklog("user@example.com", videoGameId))
                    .thenReturn(true);

            // When / Then
            mockMvc.perform(get("/api/me/backlog/{videoGameId}/status", videoGameId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inBacklog").value(true));
        }

        @Test
        @DisplayName("should return false when game is not in backlog")
        @WithMockUser(username = "user@example.com")
        void isInBacklog_shouldReturnFalse() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            when(backlogService.isInBacklog("user@example.com", videoGameId))
                    .thenReturn(false);

            // When / Then
            mockMvc.perform(get("/api/me/backlog/{videoGameId}/status", videoGameId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inBacklog").value(false));
        }
    }
}
