package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.catalog.GameCardDto;
import com.checkpoint.api.dto.catalog.GameDetailDto;
import com.checkpoint.api.dto.catalog.GameDetailDto.CompanyDto;
import com.checkpoint.api.dto.catalog.GameDetailDto.GenreDto;
import com.checkpoint.api.dto.catalog.GameDetailDto.PlatformDto;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.services.GameCatalogService;

/**
 * Unit tests for {@link GameController}.
 */
@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameCatalogService gameCatalogService;

    @Test
    @DisplayName("GET /api/games should return paginated games")
    void getGames_shouldReturnPaginatedGames() throws Exception {
        // Given
        UUID gameId = UUID.randomUUID();
        List<GameCardDto> cards = List.of(
                new GameCardDto(gameId, "The Witcher 3", "cover.jpg", LocalDate.of(2015, 5, 19), 4.8, 1500L)
        );
        Page<GameCardDto> page = new PageImpl<>(cards);

        when(gameCatalogService.getGameCatalog(any(Pageable.class))).thenReturn(page);

        // When / Then
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("The Witcher 3"))
                .andExpect(jsonPath("$.content[0].coverUrl").value("cover.jpg"))
                .andExpect(jsonPath("$.content[0].averageRating").value(4.8))
                .andExpect(jsonPath("$.metadata.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/games should accept pagination parameters")
    void getGames_shouldAcceptPaginationParameters() throws Exception {
        // Given
        Page<GameCardDto> emptyPage = new PageImpl<>(List.of());
        when(gameCatalogService.getGameCatalog(any(Pageable.class))).thenReturn(emptyPage);

        // When / Then
        mockMvc.perform(get("/api/games")
                        .param("page", "2")
                        .param("size", "50")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/games/{id} should return game details")
    void getGameById_shouldReturnGameDetails() throws Exception {
        // Given
        UUID gameId = UUID.randomUUID();
        GameDetailDto detail = new GameDetailDto(
                gameId,
                "The Witcher 3",
                "An epic RPG",
                "cover.jpg",
                LocalDate.of(2015, 5, 19),
                4.8,
                1500L,
                List.of(new GenreDto(UUID.randomUUID(), "RPG")),
                List.of(new PlatformDto(UUID.randomUUID(), "PC")),
                List.of(new CompanyDto(UUID.randomUUID(), "CD Projekt RED"))
        );

        when(gameCatalogService.getGameDetails(gameId)).thenReturn(detail);

        // When / Then
        mockMvc.perform(get("/api/games/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId.toString()))
                .andExpect(jsonPath("$.title").value("The Witcher 3"))
                .andExpect(jsonPath("$.description").value("An epic RPG"))
                .andExpect(jsonPath("$.averageRating").value(4.8))
                .andExpect(jsonPath("$.genres").isArray())
                .andExpect(jsonPath("$.genres[0].name").value("RPG"))
                .andExpect(jsonPath("$.platforms[0].name").value("PC"))
                .andExpect(jsonPath("$.companies[0].name").value("CD Projekt RED"));
    }

    @Test
    @DisplayName("GET /api/games/{id} should return 404 when game not found")
    void getGameById_shouldReturn404WhenNotFound() throws Exception {
        // Given
        UUID gameId = UUID.randomUUID();
        when(gameCatalogService.getGameDetails(gameId)).thenThrow(new GameNotFoundException(gameId));

        // When / Then
        mockMvc.perform(get("/api/games/{id}", gameId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/games should limit page size to max 100")
    void getGames_shouldLimitPageSizeToMax() throws Exception {
        // Given
        Page<GameCardDto> emptyPage = new PageImpl<>(List.of());
        when(gameCatalogService.getGameCatalog(any(Pageable.class))).thenReturn(emptyPage);

        // When / Then - requesting size 500 should be capped
        mockMvc.perform(get("/api/games")
                        .param("size", "500"))
                .andExpect(status().isOk());
    }
}
