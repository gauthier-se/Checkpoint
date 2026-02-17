package com.checkpoint.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.UserMeDto;
import com.checkpoint.api.security.ApiAuthenticationEntryPoint;
import com.checkpoint.api.security.JwtAuthenticationFilter;
import com.checkpoint.api.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link AuthController}.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should return JWT token for Desktop client (via header)")
        void shouldReturnTokenForDesktopClient() throws Exception {
            // Given
            when(authService.authenticateAndGenerateToken(any(LoginRequestDto.class)))
                    .thenReturn("jwt.token.here");

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Client-Type", "Desktop")
                            .content("""
                                    {"email": "user@test.com", "password": "password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"));

            verify(authService).authenticateAndGenerateToken(any(LoginRequestDto.class));
            verify(authService, never()).authenticateAndCreateSession(
                    any(), any(HttpServletRequest.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("Should create session for Web client (no header)")
        void shouldCreateSessionForWebClient() throws Exception {
            // Given
            doNothing().when(authService).authenticateAndCreateSession(
                    any(LoginRequestDto.class), any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "user@test.com", "password": "password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Login successful"));

            verify(authService).authenticateAndCreateSession(
                    any(LoginRequestDto.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
            verify(authService, never()).authenticateAndGenerateToken(any());
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials (Desktop)")
        void shouldReturn401ForInvalidDesktopCredentials() throws Exception {
            // Given
            when(authService.authenticateAndGenerateToken(any(LoginRequestDto.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Client-Type", "Desktop")
                            .content("""
                                    {"email": "user@test.com", "password": "wrong"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials (Web)")
        void shouldReturn401ForInvalidWebCredentials() throws Exception {
            // Given
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authService).authenticateAndCreateSession(
                            any(LoginRequestDto.class), any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "user@test.com", "password": "wrong"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for missing email")
        void shouldReturn400ForMissingEmail() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"password": "password123"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmail() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "not-an-email", "password": "password123"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing password")
        void shouldReturn400ForMissingPassword() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "user@test.com"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for empty body")
        void shouldReturn400ForEmptyBody() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle case-insensitive X-Client-Type header")
        void shouldHandleCaseInsensitiveHeader() throws Exception {
            // Given
            when(authService.authenticateAndGenerateToken(any(LoginRequestDto.class)))
                    .thenReturn("jwt.token.here");

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Client-Type", "desktop")
                            .content("""
                                    {"email": "user@test.com", "password": "password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/token")
    class TokenTests {

        @Test
        @DisplayName("Should return JWT token")
        void shouldReturnToken() throws Exception {
            // Given
            when(authService.authenticateAndGenerateToken(any(LoginRequestDto.class)))
                    .thenReturn("jwt.token.here");

            // When / Then
            mockMvc.perform(post("/api/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "user@test.com", "password": "password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"));
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            // Given
            when(authService.authenticateAndGenerateToken(any(LoginRequestDto.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When / Then
            mockMvc.perform(post("/api/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "user@test.com", "password": "wrong"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for missing fields")
        void shouldReturn400ForMissingFields() throws Exception {
            mockMvc.perform(post("/api/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("Should return success message on logout")
        void shouldReturnSuccessOnLogout() throws Exception {
            // Given
            doNothing().when(authService).logoutSession(
                    any(HttpServletRequest.class), any(HttpServletResponse.class));

            // When / Then
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logout successful"));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class MeTests {

        @Test
        @DisplayName("Should return current user profile with role")
        @WithMockUser(username = "alice@test.com")
        void shouldReturnCurrentUserProfile() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            UserMeDto userMeDto = new UserMeDto(userId, "alice", "alice@test.com", "ADMIN");

            when(authService.getCurrentUser("alice@test.com")).thenReturn(userMeDto);

            // When / Then
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId.toString()))
                    .andExpect(jsonPath("$.username").value("alice"))
                    .andExpect(jsonPath("$.email").value("alice@test.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            verify(authService).getCurrentUser("alice@test.com");
        }

        @Test
        @DisplayName("Should return user profile with USER role")
        @WithMockUser(username = "bob@test.com")
        void shouldReturnUserWithDefaultRole() throws Exception {
            // Given
            UUID userId = UUID.randomUUID();
            UserMeDto userMeDto = new UserMeDto(userId, "bob", "bob@test.com", "USER");

            when(authService.getCurrentUser("bob@test.com")).thenReturn(userMeDto);

            // When / Then
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("Should return 401 when user not found in database")
        @WithMockUser(username = "unknown@test.com")
        void shouldReturn401WhenUserNotFound() throws Exception {
            // Given
            when(authService.getCurrentUser("unknown@test.com"))
                    .thenThrow(new UsernameNotFoundException("User not found with email: unknown@test.com"));

            // When / Then
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
