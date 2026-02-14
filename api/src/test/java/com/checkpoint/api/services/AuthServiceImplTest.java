package com.checkpoint.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.security.JwtService;
import com.checkpoint.api.services.impl.AuthServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("authenticateAndGenerateToken")
    class AuthenticateAndGenerateTokenTests {

        @Test
        @DisplayName("Should authenticate and return JWT token")
        void shouldAuthenticateAndReturnToken() {
            // Given
            LoginRequestDto request = new LoginRequestDto("user@test.com", "password123");
            UserDetails userDetails = User.builder()
                    .username("user@test.com")
                    .password("encodedPassword")
                    .roles("USER")
                    .build();
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userDetailsService.loadUserByUsername("user@test.com"))
                    .thenReturn(userDetails);
            when(jwtService.generateToken(userDetails))
                    .thenReturn("jwt.token.here");

            // When
            String token = authService.authenticateAndGenerateToken(request);

            // Then
            assertThat(token).isEqualTo("jwt.token.here");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userDetailsService).loadUserByUsername("user@test.com");
            verify(jwtService).generateToken(userDetails);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void shouldThrowForInvalidCredentials() {
            // Given
            LoginRequestDto request = new LoginRequestDto("user@test.com", "wrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When / Then
            assertThatThrownBy(() -> authService.authenticateAndGenerateToken(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Bad credentials");

            verify(userDetailsService, never()).loadUserByUsername(any());
            verify(jwtService, never()).generateToken(any(UserDetails.class));
        }
    }

    @Nested
    @DisplayName("authenticateAndCreateSession")
    class AuthenticateAndCreateSessionTests {

        @Test
        @DisplayName("Should authenticate and create session")
        void shouldAuthenticateAndCreateSession() {
            // Given
            LoginRequestDto request = new LoginRequestDto("user@test.com", "password123");
            Authentication authentication = mock(Authentication.class);
            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            HttpServletResponse servletResponse = mock(HttpServletResponse.class);
            HttpSession session = mock(HttpSession.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(servletRequest.getSession(true)).thenReturn(session);

            // When
            authService.authenticateAndCreateSession(request, servletRequest, servletResponse);

            // Then
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(servletRequest).getSession(true);
            verify(session).setAttribute(any(), any());
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void shouldThrowForInvalidCredentials() {
            // Given
            LoginRequestDto request = new LoginRequestDto("user@test.com", "wrongPassword");
            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            HttpServletResponse servletResponse = mock(HttpServletResponse.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When / Then
            assertThatThrownBy(() -> authService.authenticateAndCreateSession(
                    request, servletRequest, servletResponse))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("logoutSession")
    class LogoutSessionTests {

        @Test
        @DisplayName("Should invalidate existing session on logout")
        void shouldInvalidateSession() {
            // Given
            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            HttpServletResponse servletResponse = mock(HttpServletResponse.class);
            HttpSession session = mock(HttpSession.class);

            when(servletRequest.getSession(false)).thenReturn(session);

            // When
            authService.logoutSession(servletRequest, servletResponse);

            // Then
            verify(session).invalidate();
        }

        @Test
        @DisplayName("Should handle logout when no session exists")
        void shouldHandleNoSession() {
            // Given
            HttpServletRequest servletRequest = mock(HttpServletRequest.class);
            HttpServletResponse servletResponse = mock(HttpServletResponse.class);

            when(servletRequest.getSession(false)).thenReturn(null);

            // When
            authService.logoutSession(servletRequest, servletResponse);

            // Then
            verify(servletRequest).getSession(false);
        }
    }
}
