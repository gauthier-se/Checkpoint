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

import com.checkpoint.api.dto.list.GameListCardDto;
import com.checkpoint.api.dto.list.GameListDetailDto;
import com.checkpoint.api.exceptions.GameAlreadyInListException;
import com.checkpoint.api.exceptions.GameListNotFoundException;
import com.checkpoint.api.exceptions.GameNotInListException;
import com.checkpoint.api.exceptions.UnauthorizedListAccessException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.GameListService;

@WebMvcTest(UserGameListController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserGameListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameListService gameListService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("POST /api/me/lists")
    class CreateList {

        @Test
        @DisplayName("should create a list and return 201")
        @WithMockUser(username = "user@example.com")
        void createList_shouldReturn201() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            GameListDetailDto response = new GameListDetailDto(
                    listId, "My Favorites", "Best games ever", false,
                    0, 0L, "testuser", null,
                    List.of(), true, false,
                    LocalDateTime.now(), LocalDateTime.now());

            when(gameListService.createList(eq("user@example.com"), any()))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/me/lists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "title": "My Favorites",
                                        "description": "Best games ever",
                                        "isPrivate": false
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(listId.toString()))
                    .andExpect(jsonPath("$.title").value("My Favorites"))
                    .andExpect(jsonPath("$.isOwner").value(true));
        }

        @Test
        @DisplayName("should return 400 when title is blank")
        @WithMockUser(username = "user@example.com")
        void createList_shouldReturn400WhenTitleBlank() throws Exception {
            // When / Then
            mockMvc.perform(post("/api/me/lists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "title": "",
                                        "isPrivate": false
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/me/lists")
    class GetMyLists {

        @Test
        @DisplayName("should return paginated lists")
        @WithMockUser(username = "user@example.com")
        void getMyLists_shouldReturnPaginatedLists() throws Exception {
            // Given
            GameListCardDto card = new GameListCardDto(
                    UUID.randomUUID(), "My List", null, false,
                    5, 3L, "testuser", null, List.of(), LocalDateTime.now());
            Page<GameListCardDto> page = new PageImpl<>(List.of(card), PageRequest.of(0, 20), 1);

            when(gameListService.getUserLists(eq("user@example.com"), any()))
                    .thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/me/lists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("My List"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /api/me/lists/{listId}")
    class UpdateList {

        @Test
        @DisplayName("should update list and return 200")
        @WithMockUser(username = "user@example.com")
        void updateList_shouldReturn200() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            GameListDetailDto response = new GameListDetailDto(
                    listId, "Updated Title", null, false,
                    0, 0L, "testuser", null,
                    List.of(), true, false,
                    LocalDateTime.now(), LocalDateTime.now());

            when(gameListService.updateList(eq("user@example.com"), eq(listId), any()))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(put("/api/me/lists/{listId}", listId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "title": "Updated Title"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"));
        }

        @Test
        @DisplayName("should return 403 when not owner")
        @WithMockUser(username = "user@example.com")
        void updateList_shouldReturn403WhenNotOwner() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            when(gameListService.updateList(eq("user@example.com"), eq(listId), any()))
                    .thenThrow(new UnauthorizedListAccessException(listId));

            // When / Then
            mockMvc.perform(put("/api/me/lists/{listId}", listId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "title": "Updated Title"
                                    }
                                    """))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/me/lists/{listId}")
    class DeleteList {

        @Test
        @DisplayName("should delete list and return 204")
        @WithMockUser(username = "user@example.com")
        void deleteList_shouldReturn204() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            doNothing().when(gameListService).deleteList("user@example.com", listId);

            // When / Then
            mockMvc.perform(delete("/api/me/lists/{listId}", listId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when list not found")
        @WithMockUser(username = "user@example.com")
        void deleteList_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            doThrow(new GameListNotFoundException(listId))
                    .when(gameListService).deleteList("user@example.com", listId);

            // When / Then
            mockMvc.perform(delete("/api/me/lists/{listId}", listId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/me/lists/{listId}/games")
    class AddGameToList {

        @Test
        @DisplayName("should add game and return 201")
        @WithMockUser(username = "user@example.com")
        void addGameToList_shouldReturn201() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            UUID videoGameId = UUID.randomUUID();
            GameListDetailDto response = new GameListDetailDto(
                    listId, "My List", null, false,
                    1, 0L, "testuser", null,
                    List.of(), true, false,
                    LocalDateTime.now(), LocalDateTime.now());

            when(gameListService.addGameToList(eq("user@example.com"), eq(listId), any()))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/me/lists/{listId}/games", listId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "videoGameId": "%s"
                                    }
                                    """, videoGameId)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.videoGamesCount").value(1));
        }

        @Test
        @DisplayName("should return 409 when game already in list")
        @WithMockUser(username = "user@example.com")
        void addGameToList_shouldReturn409WhenAlreadyInList() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            UUID videoGameId = UUID.randomUUID();

            when(gameListService.addGameToList(eq("user@example.com"), eq(listId), any()))
                    .thenThrow(new GameAlreadyInListException(videoGameId));

            // When / Then
            mockMvc.perform(post("/api/me/lists/{listId}/games", listId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "videoGameId": "%s"
                                    }
                                    """, videoGameId)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("DELETE /api/me/lists/{listId}/games/{videoGameId}")
    class RemoveGameFromList {

        @Test
        @DisplayName("should remove game and return 204")
        @WithMockUser(username = "user@example.com")
        void removeGameFromList_shouldReturn204() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            UUID videoGameId = UUID.randomUUID();
            doNothing().when(gameListService).removeGameFromList("user@example.com", listId, videoGameId);

            // When / Then
            mockMvc.perform(delete("/api/me/lists/{listId}/games/{videoGameId}", listId, videoGameId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when game not in list")
        @WithMockUser(username = "user@example.com")
        void removeGameFromList_shouldReturn404WhenNotInList() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            UUID videoGameId = UUID.randomUUID();
            doThrow(new GameNotInListException(videoGameId))
                    .when(gameListService).removeGameFromList("user@example.com", listId, videoGameId);

            // When / Then
            mockMvc.perform(delete("/api/me/lists/{listId}/games/{videoGameId}", listId, videoGameId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/me/lists/{listId}/games/reorder")
    class ReorderGames {

        @Test
        @DisplayName("should reorder games and return 200")
        @WithMockUser(username = "user@example.com")
        void reorderGames_shouldReturn200() throws Exception {
            // Given
            UUID listId = UUID.randomUUID();
            UUID game1 = UUID.randomUUID();
            UUID game2 = UUID.randomUUID();
            GameListDetailDto response = new GameListDetailDto(
                    listId, "My List", null, false,
                    2, 0L, "testuser", null,
                    List.of(), true, false,
                    LocalDateTime.now(), LocalDateTime.now());

            when(gameListService.reorderGames(eq("user@example.com"), eq(listId), any()))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(put("/api/me/lists/{listId}/games/reorder", listId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "orderedVideoGameIds": ["%s", "%s"]
                                    }
                                    """, game2, game1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.videoGamesCount").value(2));
        }
    }
}
