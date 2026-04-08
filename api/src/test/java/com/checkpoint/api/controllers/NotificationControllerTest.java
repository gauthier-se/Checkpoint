package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.catalog.PagedResponseDto;
import com.checkpoint.api.dto.notification.NotificationResponseDto;
import com.checkpoint.api.dto.notification.UnreadCountDto;
import com.checkpoint.api.enums.NotificationType;
import com.checkpoint.api.exceptions.NotificationNotFoundException;
import com.checkpoint.api.exceptions.UnauthorizedNotificationAccessException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.NotificationService;

/**
 * Unit tests for {@link NotificationController}.
 */
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("GET /api/me/notifications")
    class GetNotifications {

        @Test
        @DisplayName("should return paginated notifications")
        @WithMockUser(username = "user@example.com")
        void getNotifications_shouldReturnPaginatedNotifications() throws Exception {
            // Given
            NotificationResponseDto notification = new NotificationResponseDto(
                    UUID.randomUUID(),
                    "senderUser",
                    "https://example.com/pic.jpg",
                    NotificationType.FOLLOW,
                    null,
                    "senderUser started following you",
                    false,
                    LocalDateTime.now()
            );

            PagedResponseDto.PageMetadata metadata = new PagedResponseDto.PageMetadata(
                    0, 20, 1, 1, true, true, false, false
            );

            PagedResponseDto<NotificationResponseDto> response = new PagedResponseDto<>(
                    List.of(notification), metadata
            );

            when(notificationService.getNotifications(eq("user@example.com"), eq(0), eq(20)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(get("/api/me/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].senderPseudo").value("senderUser"))
                    .andExpect(jsonPath("$.content[0].type").value("FOLLOW"))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1));
        }

        @Test
        @DisplayName("should accept pagination parameters")
        @WithMockUser(username = "user@example.com")
        void getNotifications_shouldAcceptPaginationParams() throws Exception {
            // Given
            PagedResponseDto.PageMetadata metadata = new PagedResponseDto.PageMetadata(
                    1, 10, 0, 0, false, true, false, true
            );

            PagedResponseDto<NotificationResponseDto> response = new PagedResponseDto<>(
                    List.of(), metadata
            );

            when(notificationService.getNotifications(eq("user@example.com"), eq(1), eq(10)))
                    .thenReturn(response);

            // When / Then
            mockMvc.perform(get("/api/me/notifications")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.metadata.page").value(1))
                    .andExpect(jsonPath("$.metadata.size").value(10));
        }
    }

    @Nested
    @DisplayName("GET /api/me/notifications/unread-count")
    class GetUnreadCount {

        @Test
        @DisplayName("should return unread count")
        @WithMockUser(username = "user@example.com")
        void getUnreadCount_shouldReturnCount() throws Exception {
            // Given
            when(notificationService.getUnreadCount(eq("user@example.com")))
                    .thenReturn(new UnreadCountDto(5));

            // When / Then
            mockMvc.perform(get("/api/me/notifications/unread-count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(5));
        }
    }

    @Nested
    @DisplayName("PUT /api/me/notifications/{id}/read")
    class MarkAsRead {

        @Test
        @DisplayName("should return 204 when marking notification as read")
        @WithMockUser(username = "user@example.com")
        void markAsRead_shouldReturn204() throws Exception {
            // Given
            UUID notificationId = UUID.randomUUID();
            doNothing().when(notificationService).markAsRead(eq(notificationId), eq("user@example.com"));

            // When / Then
            mockMvc.perform(put("/api/me/notifications/{id}/read", notificationId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when notification not found")
        @WithMockUser(username = "user@example.com")
        void markAsRead_shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID notificationId = UUID.randomUUID();
            doThrow(new NotificationNotFoundException(notificationId))
                    .when(notificationService).markAsRead(eq(notificationId), eq("user@example.com"));

            // When / Then
            mockMvc.perform(put("/api/me/notifications/{id}/read", notificationId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 403 when notification belongs to another user")
        @WithMockUser(username = "user@example.com")
        void markAsRead_shouldReturn403WhenNotOwned() throws Exception {
            // Given
            UUID notificationId = UUID.randomUUID();
            doThrow(new UnauthorizedNotificationAccessException(notificationId))
                    .when(notificationService).markAsRead(eq(notificationId), eq("user@example.com"));

            // When / Then
            mockMvc.perform(put("/api/me/notifications/{id}/read", notificationId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/me/notifications/read-all")
    class MarkAllAsRead {

        @Test
        @DisplayName("should return 204 when marking all as read")
        @WithMockUser(username = "user@example.com")
        void markAllAsRead_shouldReturn204() throws Exception {
            // Given
            doNothing().when(notificationService).markAllAsRead(eq("user@example.com"));

            // When / Then
            mockMvc.perform(put("/api/me/notifications/read-all"))
                    .andExpect(status().isNoContent());
        }
    }
}
