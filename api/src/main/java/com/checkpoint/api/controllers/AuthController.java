package com.checkpoint.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.auth.AuthMessageDto;
import com.checkpoint.api.dto.auth.ForgotPasswordRequestDto;
import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.LoginResponseDto;
import com.checkpoint.api.dto.auth.RefreshTokenRequestDto;
import com.checkpoint.api.dto.auth.RegisterRequestDto;
import com.checkpoint.api.dto.auth.ResetPasswordRequestDto;
import com.checkpoint.api.dto.auth.TokenPairDto;
import com.checkpoint.api.dto.auth.UserMeDto;
import com.checkpoint.api.services.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Authentication controller supporting two client types:
 *
 * <ul>
 *   <li><strong>Desktop</strong> ({@code X-Client-Type: Desktop} header or
 *       {@code POST /api/auth/token}): returns a JWT access token and a refresh token in the response body.</li>
 *   <li><strong>Web</strong> (default): sets {@code checkpoint_token} (access) and
 *       {@code checkpoint_refresh} (refresh) HttpOnly cookies.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Unified login endpoint.
     *
     * <p>Desktop clients (identified by the {@code X-Client-Type: Desktop} header) receive a
     * {@link TokenPairDto} in the response body. Web clients receive both cookies.</p>
     *
     * @param loginRequest    the login credentials
     * @param clientType      optional header to specify the client type
     * @param servletResponse the HTTP servlet response (used to write cookies for Web clients)
     * @return token pair (Desktop) or success message (Web)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            HttpServletResponse servletResponse) {

        if ("Desktop".equalsIgnoreCase(clientType)) {
            TokenPairDto pair = authService.authenticateAndGenerateTokenPair(loginRequest);
            return ResponseEntity.ok(pair);
        }

        authService.authenticateAndSetCookie(loginRequest, servletResponse);
        return ResponseEntity.ok(new AuthMessageDto("Login successful"));
    }

    /**
     * Endpoint for user registration.
     *
     * @param registerRequest the registration details
     * @return 201 Created on success
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequestDto registerRequest) {

        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Dedicated token endpoint for Desktop clients.
     * Returns a {@link TokenPairDto} containing both the access and refresh tokens.
     *
     * @param loginRequest the login credentials
     * @return token pair in the response body
     */
    @PostMapping("/token")
    public ResponseEntity<TokenPairDto> token(
            @Valid @RequestBody LoginRequestDto loginRequest) {

        TokenPairDto pair = authService.authenticateAndGenerateTokenPair(loginRequest);
        return ResponseEntity.ok(pair);
    }

    /**
     * Token refresh endpoint.
     *
     * <p>Web clients send the {@code checkpoint_refresh} cookie; the response sets new
     * {@code checkpoint_token} and {@code checkpoint_refresh} cookies (token rotation).</p>
     *
     * <p>Desktop clients ({@code X-Client-Type: Desktop}) send the refresh token in the
     * request body and receive a new {@link TokenPairDto}.</p>
     *
     * @param clientType      optional header to identify Desktop clients
     * @param refreshCookie   the {@code checkpoint_refresh} cookie value (Web clients)
     * @param body            the refresh token request body (Desktop clients)
     * @param servletResponse the HTTP servlet response to write new cookies on (Web clients)
     * @return token pair (Desktop) or success message (Web)
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @CookieValue(value = "checkpoint_refresh", required = false) String refreshCookie,
            @RequestBody(required = false) RefreshTokenRequestDto body,
            HttpServletResponse servletResponse) {

        if ("Desktop".equalsIgnoreCase(clientType)) {
            String token = (body != null) ? body.refreshToken() : null;
            TokenPairDto pair = authService.refreshTokenForDesktop(token);
            return ResponseEntity.ok(pair);
        }

        authService.refreshTokenAndSetCookie(refreshCookie, servletResponse);
        return ResponseEntity.ok(new AuthMessageDto("Token refreshed"));
    }

    /**
     * Logout endpoint.
     *
     * <p>Revokes the refresh token, expires both cookies, and clears the security context.</p>
     *
     * @param refreshCookie   the {@code checkpoint_refresh} cookie value, or {@code null}
     * @param servletResponse the HTTP servlet response to write expired cookies on
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthMessageDto> logout(
            @CookieValue(value = "checkpoint_refresh", required = false) String refreshCookie,
            HttpServletResponse servletResponse) {
        authService.clearAuthCookie(refreshCookie, servletResponse);
        return ResponseEntity.ok(new AuthMessageDto("Logout successful"));
    }

    /**
     * Generates a short-lived JWT for WebSocket authentication.
     *
     * <p>Web clients authenticated via the {@code checkpoint_token} cookie can call
     * this endpoint to obtain a JWT for the STOMP WebSocket connection.</p>
     *
     * @param userDetails the authenticated user principal (injected by Spring Security)
     * @return JWT token in the response body
     */
    @GetMapping("/ws-token")
    public ResponseEntity<LoginResponseDto> wsToken(
            @AuthenticationPrincipal UserDetails userDetails) {

        String token = authService.generateWsToken(userDetails);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    /**
     * Returns profile information for the currently authenticated user.
     *
     * @param userDetails the authenticated user principal (injected by Spring Security)
     * @return user profile including ID, username, email, and role
     */
    @GetMapping("/me")
    public ResponseEntity<UserMeDto> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserMeDto user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    /**
     * Endpoint for requesting a password reset.
     *
     * @param request the forgot password request
     * @return 200 OK
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {

        authService.forgotPassword(request);
        return ResponseEntity.ok(new AuthMessageDto("If the email exists, a password reset link has been logged."));
    }

    /**
     * Endpoint for resetting a password.
     *
     * @param request the reset password request
     * @return 200 OK
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthMessageDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(new AuthMessageDto("Password has been reset successfully."));
    }
}
