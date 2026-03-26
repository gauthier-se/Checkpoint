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

import com.checkpoint.api.dto.catalog.PlatformCatalogDto;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.PlatformCatalogService;

/**
 * Unit tests for {@link PlatformController}.
 */
@WebMvcTest(PlatformController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlatformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlatformCatalogService platformCatalogService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/platforms should return list of platforms sorted by name")
    void getAllPlatforms_shouldReturnPlatformList() throws Exception {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<PlatformCatalogDto> platforms = List.of(
                new PlatformCatalogDto(id1, "Nintendo Switch", 12),
                new PlatformCatalogDto(id2, "PlayStation 5", 30)
        );

        when(platformCatalogService.getAllPlatforms()).thenReturn(platforms);

        // When / Then
        mockMvc.perform(get("/api/platforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].name").value("Nintendo Switch"))
                .andExpect(jsonPath("$[0].videoGamesCount").value(12))
                .andExpect(jsonPath("$[1].name").value("PlayStation 5"))
                .andExpect(jsonPath("$[1].videoGamesCount").value(30));
    }

    @Test
    @DisplayName("GET /api/platforms should return empty list when no platforms exist")
    void getAllPlatforms_shouldReturnEmptyList() throws Exception {
        // Given
        when(platformCatalogService.getAllPlatforms()).thenReturn(List.of());

        // When / Then
        mockMvc.perform(get("/api/platforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
