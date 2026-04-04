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

import com.checkpoint.api.dto.tag.TagRequestDto;
import com.checkpoint.api.dto.tag.TagResponseDto;
import com.checkpoint.api.dto.playlog.GamePlayLogResponseDto;
import com.checkpoint.api.enums.PlayStatus;
import com.checkpoint.api.exceptions.DuplicateTagException;
import com.checkpoint.api.exceptions.TagNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.TagService;

/**
 * Unit tests for {@link TagController}.
 */
@WebMvcTest(TagController.class)
@AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("POST /api/me/tags")
    class CreateTag {

        @Test
        @DisplayName("should create a tag and return 201")
        @WithMockUser(username = "user@example.com")
        void createTag_shouldReturnCreated() throws Exception {
            // Given
            TagResponseDto response = new TagResponseDto(UUID.randomUUID(), "cozy", 0);
            when(tagService.createTag(eq("user@example.com"), any(TagRequestDto.class)))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/me/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"Cozy\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("cozy"))
                    .andExpect(jsonPath("$.playLogsCount").value(0));
        }

        @Test
        @DisplayName("should return 409 when tag name already exists")
        @WithMockUser(username = "user@example.com")
        void createTag_shouldReturn409WhenDuplicate() throws Exception {
            // Given
            when(tagService.createTag(eq("user@example.com"), any(TagRequestDto.class)))
                    .thenThrow(new DuplicateTagException("Tag with name 'cozy' already exists"));

            // When & Then
            mockMvc.perform(post("/api/me/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"Cozy\"}"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        @WithMockUser(username = "user@example.com")
        void createTag_shouldReturn400WhenNameBlank() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/me/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/me/tags")
    class GetUserTags {

        @Test
        @DisplayName("should return all user tags with counts")
        @WithMockUser(username = "user@example.com")
        void getUserTags_shouldReturnTags() throws Exception {
            // Given
            List<TagResponseDto> tags = List.of(
                    new TagResponseDto(UUID.randomUUID(), "cozy", 3),
                    new TagResponseDto(UUID.randomUUID(), "ps5", 5)
            );
            when(tagService.getUserTags("user@example.com")).thenReturn(tags);

            // When & Then
            mockMvc.perform(get("/api/me/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("cozy"))
                    .andExpect(jsonPath("$[1].playLogsCount").value(5));
        }
    }

    @Nested
    @DisplayName("PUT /api/me/tags/{tagId}")
    class UpdateTag {

        @Test
        @DisplayName("should rename a tag and return 200")
        @WithMockUser(username = "user@example.com")
        void updateTag_shouldReturnUpdatedTag() throws Exception {
            // Given
            UUID tagId = UUID.randomUUID();
            TagResponseDto response = new TagResponseDto(tagId, "relaxing", 3);
            when(tagService.updateTag(eq("user@example.com"), eq(tagId), any(TagRequestDto.class)))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(put("/api/me/tags/{tagId}", tagId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"Relaxing\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("relaxing"));
        }

        @Test
        @DisplayName("should return 404 when tag not found")
        @WithMockUser(username = "user@example.com")
        void updateTag_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID tagId = UUID.randomUUID();
            when(tagService.updateTag(eq("user@example.com"), eq(tagId), any(TagRequestDto.class)))
                    .thenThrow(new TagNotFoundException("Tag not found with ID: " + tagId));

            // When & Then
            mockMvc.perform(put("/api/me/tags/{tagId}", tagId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"Relaxing\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/me/tags/{tagId}")
    class DeleteTag {

        @Test
        @DisplayName("should delete a tag and return 204")
        @WithMockUser(username = "user@example.com")
        void deleteTag_shouldReturnNoContent() throws Exception {
            // Given
            UUID tagId = UUID.randomUUID();
            doNothing().when(tagService).deleteTag("user@example.com", tagId);

            // When & Then
            mockMvc.perform(delete("/api/me/tags/{tagId}", tagId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when tag not found")
        @WithMockUser(username = "user@example.com")
        void deleteTag_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID tagId = UUID.randomUUID();
            doThrow(new TagNotFoundException("Tag not found with ID: " + tagId))
                    .when(tagService).deleteTag("user@example.com", tagId);

            // When & Then
            mockMvc.perform(delete("/api/me/tags/{tagId}", tagId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/me/tags/{tagId}/plays")
    class GetPlayLogsByTag {

        @Test
        @DisplayName("should return paginated play logs for a tag")
        @WithMockUser(username = "user@example.com")
        void getPlayLogsByTag_shouldReturnPaginatedPlayLogs() throws Exception {
            // Given
            UUID tagId = UUID.randomUUID();
            GamePlayLogResponseDto playLog = new GamePlayLogResponseDto(
                    UUID.randomUUID(), UUID.randomUUID(), "Zelda", null,
                    UUID.randomUUID(), "Switch", PlayStatus.COMPLETED,
                    false, 120, null, null, "owned",
                    null, null, false, null, 5, List.of()
            );
            Page<GamePlayLogResponseDto> page = new PageImpl<>(
                    List.of(playLog), PageRequest.of(0, 20), 1);
            when(tagService.getPlayLogsByTag(eq("user@example.com"), eq(tagId), any()))
                    .thenReturn(page);

            // When & Then
            mockMvc.perform(get("/api/me/tags/{tagId}/plays", tagId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Zelda"));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{username}/tags")
    class GetPublicUserTags {

        @Test
        @DisplayName("should return public user tags")
        void getPublicUserTags_shouldReturnTags() throws Exception {
            // Given
            List<TagResponseDto> tags = List.of(
                    new TagResponseDto(UUID.randomUUID(), "platine", 2)
            );
            when(tagService.getPublicUserTags("testuser")).thenReturn(tags);

            // When & Then
            mockMvc.perform(get("/api/users/{username}/tags", "testuser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("platine"));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{username}/tags/{tagName}/games")
    class GetPublicPlayLogsByTag {

        @Test
        @DisplayName("should return paginated play logs for a public tag")
        void getPublicPlayLogsByTag_shouldReturnPaginatedPlayLogs() throws Exception {
            // Given
            GamePlayLogResponseDto playLog = new GamePlayLogResponseDto(
                    UUID.randomUUID(), UUID.randomUUID(), "Elden Ring", null,
                    UUID.randomUUID(), "PS5", PlayStatus.COMPLETED,
                    false, 200, null, null, "owned",
                    null, null, false, null, 5, List.of()
            );
            Page<GamePlayLogResponseDto> page = new PageImpl<>(
                    List.of(playLog), PageRequest.of(0, 20), 1);
            when(tagService.getPublicPlayLogsByTag(eq("testuser"), eq("platine"), any()))
                    .thenReturn(page);

            // When & Then
            mockMvc.perform(get("/api/users/{username}/tags/{tagName}/games", "testuser", "platine"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Elden Ring"));
        }
    }
}
