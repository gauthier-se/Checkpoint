package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.playlog.PlayLogDetailDto;
import com.checkpoint.api.dto.playlog.ReviewSummaryDto;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.exceptions.PlayLogNotFoundException;
import com.checkpoint.api.exceptions.ProfilePrivateException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.PlayLogService;

/**
 * Unit tests for {@link PlayLogController}.
 */
@WebMvcTest(PlayLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlayLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayLogService playLogService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    private PlayLogDetailDto buildDetail(UUID id, UUID gameId, UUID userId, boolean withReview) {
        ReviewSummaryDto review = withReview
                ? new ReviewSummaryDto(
                        UUID.randomUUID(),
                        "Great game!",
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        3L,
                        2L,
                        false
                )
                : null;
        return new PlayLogDetailDto(
                id,
                LocalDateTime.now(),
                LocalDateTime.now(),
                gameId,
                "The Witcher 3",
                "cover.jpg",
                LocalDate.of(2015, 5, 19),
                userId,
                "geralt",
                null,
                PlayStatus.COMPLETED,
                false,
                3000,
                LocalDate.now().minusDays(10),
                LocalDate.now(),
                "owned",
                UUID.randomUUID(),
                "PC",
                9,
                List.of(),
                review,
                false,
                false
        );
    }

    @Nested
    @DisplayName("GET /api/v1/plays/{playId}")
    class GetPlayLogDetail {

        @Test
        @DisplayName("should return play log detail when found")
        void getPlayLogDetail_shouldReturn200() throws Exception {
            UUID playId = UUID.randomUUID();
            UUID gameId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(playLogService.getPlayLogDetail(eq(playId), eq(null)))
                    .thenReturn(buildDetail(playId, gameId, userId, true));

            mockMvc.perform(get("/api/v1/plays/{playId}", playId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(playId.toString()))
                    .andExpect(jsonPath("$.title").value("The Witcher 3"))
                    .andExpect(jsonPath("$.username").value("geralt"))
                    .andExpect(jsonPath("$.score").value(9))
                    .andExpect(jsonPath("$.review.content").value("Great game!"))
                    .andExpect(jsonPath("$.review.likeCount").value(3))
                    .andExpect(jsonPath("$.review.commentCount").value(2));
        }

        @Test
        @DisplayName("should omit review when absent")
        void getPlayLogDetail_shouldOmitReviewWhenAbsent() throws Exception {
            UUID playId = UUID.randomUUID();

            when(playLogService.getPlayLogDetail(eq(playId), eq(null)))
                    .thenReturn(buildDetail(playId, UUID.randomUUID(), UUID.randomUUID(), false));

            mockMvc.perform(get("/api/v1/plays/{playId}", playId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.review").doesNotExist());
        }

        @Test
        @DisplayName("should pass viewer email when authenticated")
        @WithMockUser(username = "viewer@example.com")
        void getPlayLogDetail_shouldForwardViewerEmail() throws Exception {
            UUID playId = UUID.randomUUID();

            when(playLogService.getPlayLogDetail(eq(playId), eq("viewer@example.com")))
                    .thenReturn(buildDetail(playId, UUID.randomUUID(), UUID.randomUUID(), false));

            mockMvc.perform(get("/api/v1/plays/{playId}", playId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 404 when play log not found")
        void getPlayLogDetail_shouldReturn404() throws Exception {
            UUID playId = UUID.randomUUID();

            when(playLogService.getPlayLogDetail(eq(playId), eq(null)))
                    .thenThrow(new PlayLogNotFoundException("Play log not found with ID: " + playId));

            mockMvc.perform(get("/api/v1/plays/{playId}", playId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 when owner profile is private")
        void getPlayLogDetail_shouldReturn403WhenPrivate() throws Exception {
            UUID playId = UUID.randomUUID();

            when(playLogService.getPlayLogDetail(eq(playId), eq(null)))
                    .thenThrow(new ProfilePrivateException("geralt"));

            mockMvc.perform(get("/api/v1/plays/{playId}", playId))
                    .andExpect(status().isForbidden());
        }
    }
}
