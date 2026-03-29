package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.list.GameListCardDto;
import com.checkpoint.api.dto.list.GameListDetailDto;
import com.checkpoint.api.dto.list.GameListEntryDto;
import com.checkpoint.api.exceptions.GameListNotFoundException;
import com.checkpoint.api.exceptions.UnauthorizedListAccessException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.GameListService;

@WebMvcTest(GameListController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameListService gameListService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("GET /api/lists")
    class GetRecentLists {

        @Test
        @DisplayName("should return paginated recent public lists")
        void getRecentLists_shouldReturnPaginatedLists() throws Exception {
            // Given
            GameListCardDto card = new GameListCardDto(
                    UUID.randomUUID(), "Top RPGs", "Best RPGs of all time", false,
                    10, 42L, "gamer123", null,
                    List.of("cover1.jpg", "cover2.jpg"), LocalDateTime.now());
            Page<GameListCardDto> page = new PageImpl<>(List.of(card), PageRequest.of(0, 20), 1);

            when(gameListService.getRecentPublicLists(any())).thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/lists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Top RPGs"))
                    .andExpect(jsonPath("$.content[0].likesCount").value(42))
                    .andExpect(jsonPath("$.content[0].coverUrls[0]").value("cover1.jpg"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/lists/popular")
    class GetPopularLists {

        @Test
        @DisplayName("should return paginated popular public lists")
        void getPopularLists_shouldReturnPaginatedLists() throws Exception {
            // Given
            GameListCardDto card = new GameListCardDto(
                    UUID.randomUUID(), "Most Liked", null, false,
                    5, 100L, "curator", null, List.of(), LocalDateTime.now());
            Page<GameListCardDto> page = new PageImpl<>(List.of(card), PageRequest.of(0, 20), 1);

            when(gameListService.getPopularPublicLists(any())).thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/lists/popular"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Most Liked"))
                    .andExpect(jsonPath("$.content[0].likesCount").value(100));
        }
    }

    @Nested
    @DisplayName("GET /api/lists/{listId}")
    class GetListDetail {

        @Test
        @DisplayName("should return list detail for public list as anonymous")
        void getListDetail_shouldReturnDetailAnonymous() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            GameListDetailDto detail = new GameListDetailDto(
                    listId, "Top RPGs", "My favorite RPGs", false,
                    2, 5L, "gamer123", null,
                    List.of(), false, false,
                    LocalDateTime.now(), LocalDateTime.now());

            when(gameListService.getListDetail(eq(listId), eq(null))).thenReturn(detail);

            // When / Then
            mockMvc.perform(get("/api/lists/{listId}", listId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Top RPGs"))
                    .andExpect(jsonPath("$.isOwner").value(false))
                    .andExpect(jsonPath("$.hasLiked").value(false));
        }

        @Test
        @DisplayName("should return list detail with owner context when authenticated")
        @WithMockUser(username = "user@example.com")
        void getListDetail_shouldReturnDetailAuthenticated() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            GameListDetailDto detail = new GameListDetailDto(
                    listId, "My List", null, false,
                    1, 0L, "testuser", null,
                    List.of(), true, false,
                    LocalDateTime.now(), LocalDateTime.now());

            when(gameListService.getListDetail(eq(listId), eq("user@example.com"))).thenReturn(detail);

            // When / Then
            mockMvc.perform(get("/api/lists/{listId}", listId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isOwner").value(true));
        }

        @Test
        @DisplayName("should return 404 when list not found")
        void getListDetail_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            when(gameListService.getListDetail(eq(listId), eq(null)))
                    .thenThrow(new GameListNotFoundException(listId));

            // When / Then
            mockMvc.perform(get("/api/lists/{listId}", listId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 for private list as non-owner")
        void getListDetail_shouldReturn403ForPrivateList() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            when(gameListService.getListDetail(eq(listId), eq(null)))
                    .thenThrow(new UnauthorizedListAccessException(listId));

            // When / Then
            mockMvc.perform(get("/api/lists/{listId}", listId))
                    .andExpect(status().isForbidden());
        }
    }
}
