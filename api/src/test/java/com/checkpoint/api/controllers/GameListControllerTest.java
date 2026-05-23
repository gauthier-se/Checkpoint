package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import com.checkpoint.api.exceptions.GameListNotFoundException;
import com.checkpoint.api.exceptions.UnauthorizedListAccessException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.GameListService;
import com.checkpoint.api.services.ListSearchService;

@WebMvcTest(GameListController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameListService gameListService;

    @MockitoBean
    private ListSearchService listSearchService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("GET /api/lists")
    class GetLists {

        @Test
        @DisplayName("should return paginated public lists with default params")
        void getLists_shouldReturnPaginatedLists() throws Exception {
            GameListCardDto card = new GameListCardDto(
                    UUID.randomUUID(), "Top RPGs", "Best RPGs of all time", false,
                    10, 42L, 0L, "gamer123", null,
                    List.of("cover1.jpg", "cover2.jpg"), LocalDateTime.now());
            Page<GameListCardDto> page = new PageImpl<>(List.of(card), PageRequest.of(0, 20), 1);

            when(listSearchService.search(any(), any(), eq(null))).thenReturn(page);

            mockMvc.perform(get("/api/lists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Top RPGs"))
                    .andExpect(jsonPath("$.content[0].likesCount").value(42))
                    .andExpect(jsonPath("$.content[0].coverUrls[0]").value("cover1.jpg"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }

        @Test
        @DisplayName("should forward fuzzy query and filters to the search service")
        void getLists_shouldForwardCriteria() throws Exception {
            Page<GameListCardDto> empty = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            when(listSearchService.search(
                    argThat(c -> "beste".equals(c.q())
                            && "popular".equals(c.sort())
                            && "alice".equals(c.author())
                            && c.minGames() != null && c.minGames() == 10),
                    any(),
                    eq(null)
            )).thenReturn(empty);

            mockMvc.perform(get("/api/lists")
                            .param("q", "beste")
                            .param("sort", "popular")
                            .param("author", "alice")
                            .param("minGames", "10"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 401 when visibility=mine is requested anonymously")
        void getLists_shouldReturn401WhenMineAndAnonymous() throws Exception {
            mockMvc.perform(get("/api/lists").param("visibility", "mine"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should accept visibility=mine when authenticated")
        @WithMockUser(username = "user@example.com")
        void getLists_shouldAcceptMineWhenAuthenticated() throws Exception {
            Page<GameListCardDto> empty = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            when(listSearchService.search(
                    argThat(c -> "mine".equals(c.visibility())),
                    any(),
                    eq("user@example.com")
            )).thenReturn(empty);

            mockMvc.perform(get("/api/lists").param("visibility", "mine"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/lists/popular")
    class GetPopularLists {

        @Test
        @DisplayName("should return paginated popular public lists")
        void getPopularLists_shouldReturnPaginatedLists() throws Exception {
            GameListCardDto card = new GameListCardDto(
                    UUID.randomUUID(), "Most Liked", null, false,
                    5, 100L, 0L, "curator", null, List.of(), LocalDateTime.now());
            Page<GameListCardDto> page = new PageImpl<>(List.of(card), PageRequest.of(0, 20), 1);

            when(gameListService.getPopularPublicLists(any())).thenReturn(page);

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
            UUID listId = UUID.randomUUID();
            GameListDetailDto detail = new GameListDetailDto(
                    listId, "Top RPGs", "My favorite RPGs", false,
                    2, 5L, 0L, "gamer123", null,
                    List.of(), false, false,
                    LocalDateTime.now(), LocalDateTime.now());

            when(gameListService.getListDetail(eq(listId), eq(null))).thenReturn(detail);

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
            UUID listId = UUID.randomUUID();
            GameListDetailDto detail = new GameListDetailDto(
                    listId, "My List", null, false,
                    1, 0L, 0L, "testuser", null,
                    List.of(), true, false,
                    LocalDateTime.now(), LocalDateTime.now());

            when(gameListService.getListDetail(eq(listId), eq("user@example.com"))).thenReturn(detail);

            mockMvc.perform(get("/api/lists/{listId}", listId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isOwner").value(true));
        }

        @Test
        @DisplayName("should return 404 when list not found")
        void getListDetail_shouldReturn404WhenNotFound() throws Exception {
            UUID listId = UUID.randomUUID();
            when(gameListService.getListDetail(eq(listId), eq(null)))
                    .thenThrow(new GameListNotFoundException(listId));

            mockMvc.perform(get("/api/lists/{listId}", listId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 for private list as non-owner")
        void getListDetail_shouldReturn403ForPrivateList() throws Exception {
            UUID listId = UUID.randomUUID();
            when(gameListService.getListDetail(eq(listId), eq(null)))
                    .thenThrow(new UnauthorizedListAccessException(listId));

            mockMvc.perform(get("/api/lists/{listId}", listId))
                    .andExpect(status().isForbidden());
        }
    }
}
