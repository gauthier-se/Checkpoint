package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.collection.WishResponseDto;
import com.checkpoint.api.exceptions.GameAlreadyInWishlistException;
import com.checkpoint.api.exceptions.GameNotFoundException;
import com.checkpoint.api.exceptions.GameNotInWishlistException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.WishlistService;

/**
 * Unit tests for {@link WishlistController}.
 */
@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("POST /api/me/wishlist/{videoGameId}")
    class AddToWishlist {

        @Test
        @DisplayName("should add game to wishlist and return 201")
        @WithMockUser(username = "user@example.com")
        void addToWishlist_shouldReturn201() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID wishId = UUID.randomUUID();
            WishResponseDto response = new WishResponseDto(
                    wishId, videoGameId, "The Witcher 3", "cover.jpg",
                    LocalDate.of(2015, 5, 19), LocalDateTime.now());

            when(wishlistService.addToWishlist(eq("user@example.com"), eq(videoGameId)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(post("/api/me/wishlist/{videoGameId}", videoGameId))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(wishId.toString()))
                    .andExpect(jsonPath("$.videoGameId").value(videoGameId.toString()))
                    .andExpect(jsonPath("$.title").value("The Witcher 3"));
        }

        @Test
        @DisplayName("should return 409 when game already in wishlist")
        @WithMockUser(username = "user@example.com")
        void addToWishlist_shouldReturn409WhenAlreadyInWishlist() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();

            when(wishlistService.addToWishlist(eq("user@example.com"), eq(videoGameId)))
                    .thenThrow(new GameAlreadyInWishlistException(videoGameId));

            // When / Then
            mockMvc.perform(post("/api/me/wishlist/{videoGameId}", videoGameId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"));
        }

        @Test
        @DisplayName("should return 404 when video game not found")
        @WithMockUser(username = "user@example.com")
        void addToWishlist_shouldReturn404WhenGameNotFound() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();

            when(wishlistService.addToWishlist(eq("user@example.com"), eq(videoGameId)))
                    .thenThrow(new GameNotFoundException(videoGameId));

            // When / Then
            mockMvc.perform(post("/api/me/wishlist/{videoGameId}", videoGameId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("DELETE /api/me/wishlist/{videoGameId}")
    class RemoveFromWishlist {

        @Test
        @DisplayName("should remove game and return 204")
        @WithMockUser(username = "user@example.com")
        void removeFromWishlist_shouldReturn204() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            doNothing().when(wishlistService)
                    .removeFromWishlist("user@example.com", videoGameId);

            // When / Then
            mockMvc.perform(delete("/api/me/wishlist/{videoGameId}", videoGameId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when game not in wishlist")
        @WithMockUser(username = "user@example.com")
        void removeFromWishlist_shouldReturn404WhenNotInWishlist() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            doThrow(new GameNotInWishlistException(videoGameId))
                    .when(wishlistService)
                    .removeFromWishlist("user@example.com", videoGameId);

            // When / Then
            mockMvc.perform(delete("/api/me/wishlist/{videoGameId}", videoGameId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/me/wishlist")
    class GetUserWishlist {

        @Test
        @DisplayName("should return paginated wishlist")
        @WithMockUser(username = "user@example.com")
        void getWishlist_shouldReturnPaginatedWishlist() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            UUID wishId = UUID.randomUUID();
            List<WishResponseDto> items = List.of(
                    new WishResponseDto(wishId, videoGameId, "Elden Ring", "cover.jpg",
                            LocalDate.of(2022, 2, 25), LocalDateTime.now())
            );
            Page<WishResponseDto> page = new PageImpl<>(items);

            when(wishlistService.getUserWishlist(eq("user@example.com"), any(Pageable.class)))
                    .thenReturn(page);

            // When / Then
            mockMvc.perform(get("/api/me/wishlist"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("Elden Ring"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }

        @Test
        @DisplayName("should accept pagination parameters")
        @WithMockUser(username = "user@example.com")
        void getWishlist_shouldAcceptPaginationParams() throws Exception {
            // Given
            Page<WishResponseDto> emptyPage = new PageImpl<>(List.of());
            when(wishlistService.getUserWishlist(eq("user@example.com"), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // When / Then
            mockMvc.perform(get("/api/me/wishlist")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sort", "createdAt,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/me/wishlist/{videoGameId}/status")
    class IsInWishlist {

        @Test
        @DisplayName("should return true when game is in wishlist")
        @WithMockUser(username = "user@example.com")
        void isInWishlist_shouldReturnTrue() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            when(wishlistService.isInWishlist("user@example.com", videoGameId))
                    .thenReturn(true);

            // When / Then
            mockMvc.perform(get("/api/me/wishlist/{videoGameId}/status", videoGameId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inWishlist").value(true));
        }

        @Test
        @DisplayName("should return false when game is not in wishlist")
        @WithMockUser(username = "user@example.com")
        void isInWishlist_shouldReturnFalse() throws Exception {
            // Given
            UUID videoGameId = UUID.randomUUID();
            when(wishlistService.isInWishlist("user@example.com", videoGameId))
                    .thenReturn(false);

            // When / Then
            mockMvc.perform(get("/api/me/wishlist/{videoGameId}/status", videoGameId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inWishlist").value(false));
        }
    }
}
