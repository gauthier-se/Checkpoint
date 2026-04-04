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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.playlog.GamePlayLogRequestDto;
import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.PlayLogNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.GamePlayLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link GamePlayLogController}.
 */
@WebMvcTest(GamePlayLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class GamePlayLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GamePlayLogService gamePlayLogService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("POST /api/me/plays")
    class LogPlay {

        @Test
        @DisplayName("should create play log and return 201")
        @WithMockUser(username = "user@example.com")
        void logPlay_shouldReturn201() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID platformId = UUID.randomUUID();
            UUID playId = UUID.randomUUID();

            GamePlayLogRequestDto request = new GamePlayLogRequestDto(
                    videoGameId, platformId, PlayStatus.ARE_PLAYING, LocalDate.now(), null, 120, "owned", false, null, null
            );

            GamePlayLogResponseDto response = new GamePlayLogResponseDto(
                    playId, videoGameId, "The Witcher 3", "cover.jpg", platformId, "PC",
                    PlayStatus.ARE_PLAYING, false, 120, LocalDate.now(), null, "owned",
                    LocalDateTime.now(), LocalDateTime.now(), null, null, null, List.of()
            );

            when(gamePlayLogService.logPlay(eq("user@example.com"), any(GamePlayLogRequestDto.class)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/me/plays")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(playId.toString()))
                    .andExpect(jsonPath("$.videoGameId").value(videoGameId.toString()))
                    .andExpect(jsonPath("$.title").value("The Witcher 3"));
        }

        @Test
        @DisplayName("should return 400 when missing required fields")
        @WithMockUser(username = "user@example.com")
        void logPlay_shouldReturn400WhenMissingFields() throws Exception {
            // Given
            GamePlayLogRequestDto request = new GamePlayLogRequestDto(
                    null, null, null, null, null, null, null, null, null, null
            );

            // When / Then
            mockMvc.perform(post("/api/me/plays")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/me/plays/{playId}")
    class UpdatePlayLog {

        @Test
        @DisplayName("should update play log and return 200")
        @WithMockUser(username = "user@example.com")
        void updatePlayLog_shouldReturn200() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID platformId = UUID.randomUUID();
            UUID playId = UUID.randomUUID();

            GamePlayLogRequestDto request = new GamePlayLogRequestDto(
                    videoGameId, platformId, PlayStatus.COMPLETED, LocalDate.now(), LocalDate.now(), 2000, "owned", false, null, null
            );

            GamePlayLogResponseDto response = new GamePlayLogResponseDto(
                    playId, videoGameId, "The Witcher 3", "cover.jpg", platformId, "PC",
                    PlayStatus.COMPLETED, false, 2000, LocalDate.now(), LocalDate.now(), "owned",
                    LocalDateTime.now(), LocalDateTime.now(), null, null, null, List.of()
            );

            when(gamePlayLogService.updatePlayLog(eq("user@example.com"), eq(playId), any(GamePlayLogRequestDto.class)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(put("/api/me/plays/{playId}", playId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.timePlayed").value(2000));
        }

        @Test
        @DisplayName("should return 404 when play log not found")
        @WithMockUser(username = "user@example.com")
        void updatePlayLog_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID platformId = UUID.randomUUID();
            UUID playId = UUID.randomUUID();

            GamePlayLogRequestDto request = new GamePlayLogRequestDto(
                    videoGameId, platformId, PlayStatus.COMPLETED, LocalDate.now(), LocalDate.now(), 2000, "owned", false, null, null
            );

            when(gamePlayLogService.updatePlayLog(eq("user@example.com"), eq(playId), any(GamePlayLogRequestDto.class)))
                    .thenThrow(new PlayLogNotFoundException("Not found"));

            // When / Then
            mockMvc.perform(put("/api/me/plays/{playId}", playId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/me/plays/{playId}")
    class DeletePlayLog {

        @Test
        @DisplayName("should delete play log and return 204")
        @WithMockUser(username = "user@example.com")
        void deletePlayLog_shouldReturn204() throws Exception {
            // Given
            UUID playId = UUID.randomUUID();
            doNothing().when(gamePlayLogService).deletePlayLog("user@example.com", playId);

            // When / Then
            mockMvc.perform(delete("/api/me/plays/{playId}", playId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when play log not found")
        @WithMockUser(username = "user@example.com")
        void deletePlayLog_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID playId = UUID.randomUUID();
            doThrow(new PlayLogNotFoundException("Not found"))
                    .when(gamePlayLogService).deletePlayLog("user@example.com", playId);

            // When / Then
            mockMvc.perform(delete("/api/me/plays/{playId}", playId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/me/plays")
    class GetUserPlayLogs {

        @Test
        @DisplayName("should return paginated play logs")
        @WithMockUser(username = "user@example.com")
        void getPlayLogs_shouldReturnPaginatedLogs() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID platformId = UUID.randomUUID();
            UUID playId = UUID.randomUUID();

            GamePlayLogResponseDto response = new GamePlayLogResponseDto(
                    playId, videoGameId, "The Witcher 3", "cover.jpg", platformId, "PC",
                    PlayStatus.COMPLETED, false, 2000, LocalDate.now(), LocalDate.now(), "owned",
                    LocalDateTime.now(), LocalDateTime.now(), null, null, null, List.of()
            );

            Page<GamePlayLogResponseDto> page = new PageImpl<>(List.of(response));

            when(gamePlayLogService.getUserPlayLog(eq("user@example.com"), any(Pageable.class)))
                    .thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/me/plays"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("The Witcher 3"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/me/plays/game/{videoGameId}")
    class GetGamePlayHistory {

        @Test
        @DisplayName("should return play history for game")
        @WithMockUser(username = "user@example.com")
        void getGamePlayHistory_shouldReturnList() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID platformId = UUID.randomUUID();
            UUID playId = UUID.randomUUID();

            GamePlayLogResponseDto response = new GamePlayLogResponseDto(
                    playId, videoGameId, "The Witcher 3", "cover.jpg", platformId, "PC",
                    PlayStatus.COMPLETED, false, 2000, LocalDate.now(), LocalDate.now(), "owned",
                    LocalDateTime.now(), LocalDateTime.now(), null, null, null, List.of()
            );

            when(gamePlayLogService.getGamePlayHistory("user@example.com", videoGameId))
                    .thenReturn(List.of(response));

            // When / Then
            mockMvc.perform(get("/api/me/plays/game/{videoGameId}", videoGameId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].title").value("The Witcher 3"));
        }

        @Test
        @DisplayName("should return 404 when game not found")
        @WithMockUser(username = "user@example.com")
        void getGamePlayHistory_shouldReturn404WhenGameNotFound() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();

            when(gamePlayLogService.getGamePlayHistory("user@example.com", videoGameId))
                    .thenThrow(new GameNotFoundException(videoGameId));

            // When / Then
            mockMvc.perform(get("/api/me/plays/game/{videoGameId}", videoGameId))
                    .andExpect(status().isNotFound());
        }
    }
}
