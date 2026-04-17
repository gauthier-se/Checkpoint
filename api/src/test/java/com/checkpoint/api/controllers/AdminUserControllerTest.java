package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.admin.AdminUserDetailDto;
import com.checkpoint.api.dto.admin.AdminUserDto;
import com.checkpoint.api.exceptions.UserNotFoundException;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.AdminUserService;

/**
 * Unit tests for {@link AdminUserController}.
 */
@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("GET /api/admin/users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return list of users with ban status")
        void shouldReturnListOfUsers() throws Exception {
            // Given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<AdminUserDto> users = List.of(
                    new AdminUserDto(id1, "alice", "alice@example.com", false),
                    new AdminUserDto(id2, "bob", "bob@example.com", true)
            );
            when(adminUserService.getAllUsers()).thenReturn(users);

            // When & Then
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(id1.toString()))
                    .andExpect(jsonPath("$[0].username").value("alice"))
                    .andExpect(jsonPath("$[0].banned").value(false))
                    .andExpect(jsonPath("$[1].banned").value(true));

            verify(adminUserService).getAllUsers();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() throws Exception {
            // Given
            when(adminUserService.getAllUsers()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(adminUserService).getAllUsers();
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/{id}")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return detailed user profile")
        void shouldReturnUserDetail() throws Exception {
            // Given
            UUID id = UUID.randomUUID();
            AdminUserDetailDto detail = new AdminUserDetailDto(
                    id, "alice", "alice@example.com", "A gamer", "pic.jpg",
                    false, false, 100, 5,
                    LocalDateTime.of(2025, 1, 1, 0, 0), 10L, 2L
            );
            when(adminUserService.getUserById(id)).thenReturn(detail);

            // When & Then
            mockMvc.perform(get("/api/admin/users/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.username").value("alice"))
                    .andExpect(jsonPath("$.bio").value("A gamer"))
                    .andExpect(jsonPath("$.picture").value("pic.jpg"))
                    .andExpect(jsonPath("$.banned").value(false))
                    .andExpect(jsonPath("$.xpPoint").value(100))
                    .andExpect(jsonPath("$.level").value(5))
                    .andExpect(jsonPath("$.reviewCount").value(10))
                    .andExpect(jsonPath("$.reportCount").value(2));

            verify(adminUserService).getUserById(id);
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID id = UUID.randomUUID();
            when(adminUserService.getUserById(id)).thenThrow(new UserNotFoundException(id));

            // When & Then
            mockMvc.perform(get("/api/admin/users/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/users/{id}")
    class EditUserTests {

        @Test
        @DisplayName("Should return updated user detail")
        void shouldReturnUpdatedUser() throws Exception {
            // Given
            UUID id = UUID.randomUUID();
            AdminUserDetailDto updated = new AdminUserDetailDto(
                    id, "alice", "alice@example.com", null, null,
                    true, false, 100, 5,
                    LocalDateTime.of(2025, 1, 1, 0, 0), 10L, 0L
            );
            when(adminUserService.editUser(eq(id), any())).thenReturn(updated);

            // When & Then
            mockMvc.perform(put("/api/admin/users/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"clearBio\": true, \"clearPicture\": true, \"isPrivate\": true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bio").isEmpty())
                    .andExpect(jsonPath("$.picture").isEmpty())
                    .andExpect(jsonPath("$.isPrivate").value(true));

            verify(adminUserService).editUser(eq(id), any());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID id = UUID.randomUUID();
            when(adminUserService.editUser(eq(id), any())).thenThrow(new UserNotFoundException(id));

            // When & Then
            mockMvc.perform(put("/api/admin/users/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"clearBio\": false, \"clearPicture\": false}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/users/{id}/ban")
    class BanUserTests {

        @Test
        @DisplayName("Should return 204 on successful ban")
        void shouldReturn204OnBan() throws Exception {
            // Given
            UUID id = UUID.randomUUID();

            // When & Then
            mockMvc.perform(post("/api/admin/users/{id}/ban", id))
                    .andExpect(status().isNoContent());

            verify(adminUserService).banUser(id);
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID id = UUID.randomUUID();
            doThrow(new UserNotFoundException(id)).when(adminUserService).banUser(id);

            // When & Then
            mockMvc.perform(post("/api/admin/users/{id}/ban", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/users/{id}/unban")
    class UnbanUserTests {

        @Test
        @DisplayName("Should return 204 on successful unban")
        void shouldReturn204OnUnban() throws Exception {
            // Given
            UUID id = UUID.randomUUID();

            // When & Then
            mockMvc.perform(post("/api/admin/users/{id}/unban", id))
                    .andExpect(status().isNoContent());

            verify(adminUserService).unbanUser(id);
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            UUID id = UUID.randomUUID();
            doThrow(new UserNotFoundException(id)).when(adminUserService).unbanUser(id);

            // When & Then
            mockMvc.perform(post("/api/admin/users/{id}/unban", id))
                    .andExpect(status().isNotFound());
        }
    }
}
