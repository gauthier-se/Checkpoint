package com.checkpoint.api.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.catalog.GenreCatalogDto;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.GenreCatalogService;

/**
 * Unit tests for {@link GenreController}.
 */
@WebMvcTest(GenreController.class)
@AutoConfigureMockMvc(addFilters = false)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreCatalogService genreCatalogService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/genres should return list of genres sorted by name")
    void getAllGenres_shouldReturnGenreList() throws Exception {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<GenreCatalogDto> genres = List.of(
                new GenreCatalogDto(id1, "Action", 25),
                new GenreCatalogDto(id2, "RPG", 18)
        );

        when(genreCatalogService.getAllGenres()).thenReturn(genres);

        // When / Then
        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].name").value("Action"))
                .andExpect(jsonPath("$[0].videoGamesCount").value(25))
                .andExpect(jsonPath("$[1].name").value("RPG"))
                .andExpect(jsonPath("$[1].videoGamesCount").value(18));
    }

    @Test
    @DisplayName("GET /api/genres should return empty list when no genres exist")
    void getAllGenres_shouldReturnEmptyList() throws Exception {
        // Given
        when(genreCatalogService.getAllGenres()).thenReturn(List.of());

        // When / Then
        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
