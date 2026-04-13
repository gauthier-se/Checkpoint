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

import com.fasterxml.jackson.databind.ObjectMapper;

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

import com.checkpoint.api.dto.auth.ForgotPasswordRequestDto;
import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.RegisterRequestDto;
import com.checkpoint.api.dto.auth.ResetPasswordRequestDto;
import com.checkpoint.api.dto.auth.UserMeDto;
import com.checkpoint.api.exceptions.RegistrationConflictException;
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

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should return 201 Created on successful registration")
        void shouldReturn201OnSuccess() throws Exception {
            doNothing().when(authService).register(any(RegisterRequestDto.class));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"pseudo": "newuser", "email": "newuser@test.com", "password": "password123", "confirmPassword": "password123"}
                                    """))
                    .andExpect(status().isCreated());

            verify(authService).register(any(RegisterRequestDto.class));
        }

        @Test
        @DisplayName("Should return 400 for missing fields")
        void shouldReturn400ForMissingFields() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "user@test.com"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid email")
        void shouldReturn400ForInvalidEmail() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"pseudo": "user", "email": "invalid", "password": "password123", "confirmPassword": "password123"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for short password")
        void shouldReturn400ForShortPassword() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"pseudo": "user", "email": "user@test.com", "password": "short", "confirmPassword": "short"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing confirmPassword")
        void shouldReturn400ForMissingConfirmPassword() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"pseudo": "user", "email": "user@test.com", "password": "password123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Password confirmation is required")));
        }

        @Test
        @DisplayName("Should return 400 for password mismatch")
        void shouldReturn400ForPasswordMismatch() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("Passwords do not match"))
                    .when(authService).register(any(RegisterRequestDto.class));

            // When / Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"pseudo": "user", "email": "user@test.com", "password": "password123", "confirmPassword": "different123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Passwords do not match"));
        }

        @Test
        @DisplayName("Should return 409 for duplicate email")
        void shouldReturn409ForDuplicateEmail() throws Exception {
            // Given
            doThrow(new RegistrationConflictException("Email is already in use"))
                    .when(authService).register(any(RegisterRequestDto.class));

            // When / Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"pseudo": "newuser", "email": "existing@test.com", "password": "password123", "confirmPassword": "password123"}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Email is already in use"));
        }

        @Test
        @DisplayName("Should return 409 for duplicate pseudo")
        void shouldReturn409ForDuplicatePseudo() throws Exception {
            // Given
            doThrow(new RegistrationConflictException("Pseudo is already in use"))
                    .when(authService).register(any(RegisterRequestDto.class));

            // When / Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"pseudo": "existinguser", "email": "new@test.com", "password": "password123", "confirmPassword": "password123"}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Pseudo is already in use"));
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
            UserMeDto userMeDto = new UserMeDto(userId, "alice", "alice@test.com", "ADMIN", "My bio", null, false);

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
            UserMeDto userMeDto = new UserMeDto(userId, "bob", "bob@test.com", "USER", null, null, false);

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

    @Nested
    @DisplayName("forgotPassword")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should return 200 OK on successful request")
        void shouldReturn200OnSuccess() throws Exception {
            ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("user@test.com");
            doNothing().when(authService).forgotPassword(any(ForgotPasswordRequestDto.class));

            mockMvc.perform(post("/api/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("If the email exists, a password reset link has been logged."));
        }

        @Test
        @DisplayName("Should return 400 Bad Request if email is empty")
        void shouldReturn400IfEmailEmpty() throws Exception {
            ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("");

            mockMvc.perform(post("/api/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Email is required")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request if email is invalid format")
        void shouldReturn400IfEmailInvalid() throws Exception {
            ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("invalid-email");

            mockMvc.perform(post("/api/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Email must be valid")));
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should return 200 OK on successful request")
        void shouldReturn200OnSuccess() throws Exception {
            ResetPasswordRequestDto request = new ResetPasswordRequestDto("valid-token", "new-password123");
            doNothing().when(authService).resetPassword(any(ResetPasswordRequestDto.class));

            mockMvc.perform(post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password has been reset successfully."));
        }

        @Test
        @DisplayName("Should return 400 Bad Request if token is empty")
        void shouldReturn400IfTokenEmpty() throws Exception {
            ResetPasswordRequestDto request = new ResetPasswordRequestDto("", "new-password123");

            mockMvc.perform(post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Token is required")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request if password is too short")
        void shouldReturn400IfPasswordTooShort() throws Exception {
            ResetPasswordRequestDto request = new ResetPasswordRequestDto("valid-token", "short");

            mockMvc.perform(post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Password must be at least 8 characters long")));
        }
    }
}
