package com.checkpoint.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Unit tests for {@link JwtAuthenticationFilter}.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should continue filter chain when no Authorization header is present")
    void shouldContinueWhenNoAuthHeader() throws Exception {
        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).extractUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header does not start with Bearer")
    void shouldContinueWhenNotBearerAuth() throws Exception {
        // Given
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).extractUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Should set authentication when JWT is valid")
    void shouldSetAuthenticationWhenJwtIsValid() throws Exception {
        // Given
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = new User("user@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.extractUsername(token)).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Should not set authentication when JWT is invalid")
    void shouldNotSetAuthenticationWhenJwtIsInvalid() throws Exception {
        // Given
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = new User("user@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.extractUsername(token)).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should not set authentication when JWT parsing throws exception")
    void shouldNotSetAuthenticationWhenJwtParsingFails() throws Exception {
        // Given
        String token = "malformed.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Malformed JWT"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
