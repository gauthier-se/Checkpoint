package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.admin.ExternalGameDto;
import com.checkpoint.api.entities.VideoGame;
import com.checkpoint.api.exceptions.ExternalApiUnavailableException;
import com.checkpoint.api.exceptions.ExternalGameNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.AdminGameService;

/**
 * Unit tests for {@link AdminGameController}.
 */
@WebMvcTest(AdminGameController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminGameService adminGameService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    private List<ExternalGameDto> sampleExternalGames;
    private VideoGame sampleVideoGame;

    @BeforeEach
    void setUp() {
        // Sample external games for search results
        sampleExternalGames = List.of(
                new ExternalGameDto(1942L, "The Witcher 3", 2015,
                        "https://images.igdb.com/cover1.jpg"),
                new ExternalGameDto(26226L, "The Witcher 3: Hearts of Stone", 2015,
                        "https://images.igdb.com/cover2.jpg"),
                new ExternalGameDto(26227L, "The Witcher 3: Blood and Wine", 2016,
                        "https://images.igdb.com/cover3.jpg")
        );

        // Sample video game entity
        sampleVideoGame = new VideoGame();
        sampleVideoGame.setId(UUID.randomUUID());
        sampleVideoGame.setTitle("The Witcher 3: Wild Hunt");
        sampleVideoGame.setDescription("An epic RPG adventure");
        sampleVideoGame.setReleaseDate(LocalDate.of(2015, 5, 19));
        sampleVideoGame.setCoverUrl("https://images.igdb.com/cover1.jpg");
        // Initialize empty sets to avoid NPE in mapping
        sampleVideoGame.setGenres(new HashSet<>());
        sampleVideoGame.setPlatforms(new HashSet<>());
        sampleVideoGame.setCompanies(new HashSet<>());
    }

    @Nested
    @DisplayName("GET /api/admin/external-games/search")
    class SearchExternalGamesTests {

        @Test
        @DisplayName("Should return search results for valid query")
        void shouldReturnSearchResults() throws Exception {
            when(adminGameService.searchExternalGames(eq("witcher"), anyInt()))
                    .thenReturn(sampleExternalGames);

            mockMvc.perform(get("/api/admin/external-games/search")
                            .param("query", "witcher"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].externalId").value(1942))
                    .andExpect(jsonPath("$[0].title").value("The Witcher 3"))
                    .andExpect(jsonPath("$[1].externalId").value(26226))
                    .andExpect(jsonPath("$[2].externalId").value(26227));

            verify(adminGameService).searchExternalGames("witcher", 20);
        }

        @Test
        @DisplayName("Should use custom limit when provided")
        void shouldUseCustomLimit() throws Exception {
            when(adminGameService.searchExternalGames(anyString(), anyInt()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/admin/external-games/search")
                            .param("query", "zelda")
                            .param("limit", "10"))
                    .andExpect(status().isOk());

            verify(adminGameService).searchExternalGames("zelda", 10);
        }

        @Test
        @DisplayName("Should cap limit at maximum (50)")
        void shouldCapLimitAtMaximum() throws Exception {
            when(adminGameService.searchExternalGames(anyString(), anyInt()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/admin/external-games/search")
                            .param("query", "mario")
                            .param("limit", "100"))
                    .andExpect(status().isOk());

            verify(adminGameService).searchExternalGames("mario", 50);
        }

        @Test
        @DisplayName("Should return empty list when no results")
        void shouldReturnEmptyList() throws Exception {
            when(adminGameService.searchExternalGames(anyString(), anyInt()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/admin/external-games/search")
                            .param("query", "nonexistentgame12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 400 when query parameter is missing")
        void shouldReturn400WhenQueryMissing() throws Exception {
            mockMvc.perform(get("/api/admin/external-games/search"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 503 when external API is unavailable")
        void shouldReturn503WhenExternalApiUnavailable() throws Exception {
            when(adminGameService.searchExternalGames(anyString(), anyInt()))
                    .thenThrow(new ExternalApiUnavailableException("IGDB API is unavailable"));

            mockMvc.perform(get("/api/admin/external-games/search")
                            .param("query", "zelda"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("Service Unavailable"))
                    .andExpect(jsonPath("$.message").value("IGDB API is unavailable"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/games/import/{externalId}")
    class ImportGameTests {

        @Test
        @DisplayName("Should import game successfully")
        void shouldImportGameSuccessfully() throws Exception {
            when(adminGameService.importGameByExternalId(1942L))
                    .thenReturn(sampleVideoGame);

            mockMvc.perform(post("/api/admin/games/import/1942"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(sampleVideoGame.getId().toString()))
                    .andExpect(jsonPath("$.title").value("The Witcher 3: Wild Hunt"))
                    .andExpect(jsonPath("$.releaseDate").value("2015-05-19"))
                    .andExpect(jsonPath("$.coverUrl").value("https://images.igdb.com/cover1.jpg"));

            verify(adminGameService).importGameByExternalId(1942L);
        }

        @Test
        @DisplayName("Should return 404 when external game not found")
        void shouldReturn404WhenExternalGameNotFound() throws Exception {
            when(adminGameService.importGameByExternalId(99999L))
                    .thenThrow(new ExternalGameNotFoundException(99999L));

            mockMvc.perform(post("/api/admin/games/import/99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("External game not found with ID: 99999"));
        }

        @Test
        @DisplayName("Should return 503 when external API is unavailable during import")
        void shouldReturn503WhenExternalApiUnavailableDuringImport() throws Exception {
            when(adminGameService.importGameByExternalId(1942L))
                    .thenThrow(new ExternalApiUnavailableException("IGDB API is unavailable"));

            mockMvc.perform(post("/api/admin/games/import/1942"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("Service Unavailable"));
        }
    }
}
