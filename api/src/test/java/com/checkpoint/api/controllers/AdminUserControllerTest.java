package com.checkpoint.api.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.admin.AdminUserDto;
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
        @DisplayName("Should return list of users")
        void shouldReturnListOfUsers() throws Exception {
            // Given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<AdminUserDto> users = List.of(
                    new AdminUserDto(id1, "alice", "alice@example.com"),
                    new AdminUserDto(id2, "bob", "bob@example.com")
            );
            when(adminUserService.getAllUsers()).thenReturn(users);

            // When & Then
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(id1.toString()))
                    .andExpect(jsonPath("$[0].username").value("alice"))
                    .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                    .andExpect(jsonPath("$[1].id").value(id2.toString()))
                    .andExpect(jsonPath("$[1].username").value("bob"))
                    .andExpect(jsonPath("$[1].email").value("bob@example.com"));

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

        @Test
        @DisplayName("Should return single user")
        void shouldReturnSingleUser() throws Exception {
            // Given
            UUID id = UUID.randomUUID();
            List<AdminUserDto> users = List.of(
                    new AdminUserDto(id, "admin", "admin@checkpoint.com")
            );
            when(adminUserService.getAllUsers()).thenReturn(users);

            // When & Then
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].username").value("admin"))
                    .andExpect(jsonPath("$[0].email").value("admin@checkpoint.com"));
        }

        @Test
        @DisplayName("Should return 500 when service throws unexpected exception")
        void shouldReturn500WhenServiceThrows() throws Exception {
            // Given
            when(adminUserService.getAllUsers())
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
