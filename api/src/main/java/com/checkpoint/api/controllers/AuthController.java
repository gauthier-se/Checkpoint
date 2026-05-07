package com.checkpoint.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.auth.AuthMessageDto;
import com.checkpoint.api.dto.auth.ForgotPasswordRequestDto;
import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.LoginResponseDto;
import com.checkpoint.api.dto.auth.RegisterRequestDto;
import com.checkpoint.api.dto.auth.ResetPasswordRequestDto;
import com.checkpoint.api.dto.auth.UserMeDto;
import com.checkpoint.api.services.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Authentication controller supporting two client types:
 *
 * <ul>
 *   <li><strong>Desktop</strong> ({@code X-Client-Type: Desktop} header or
 *       {@code POST /api/auth/token}): returns a JWT in the response body.</li>
 *   <li><strong>Web</strong> (default): sets a {@code checkpoint_token} HttpOnly cookie.</li>
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
     * JWT in the response body. Web clients receive a {@code checkpoint_token} HttpOnly cookie.</p>
     *
     * @param loginRequest    the login credentials
     * @param clientType      optional header to specify the client type
     * @param servletResponse the HTTP servlet response (used to write the cookie for Web clients)
     * @return JWT token body (Desktop) or success message (Web)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            @org.springframework.web.bind.annotation.RequestHeader(
                    value = "X-Client-Type", required = false) String clientType,
            HttpServletResponse servletResponse) {

        if ("Desktop".equalsIgnoreCase(clientType)) {
            String token = authService.authenticateAndGenerateToken(loginRequest);
            return ResponseEntity.ok(new LoginResponseDto(token));
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
     * Dedicated JWT token endpoint for Desktop clients.
     *
     * @param loginRequest the login credentials
     * @return JWT token in the response body
     */
    @PostMapping("/token")
    public ResponseEntity<LoginResponseDto> token(
            @Valid @RequestBody LoginRequestDto loginRequest) {

        String token = authService.authenticateAndGenerateToken(loginRequest);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    /**
     * Logout endpoint.
     *
     * <p>Expires the {@code checkpoint_token} cookie and clears the security context.</p>
     *
     * @param servletResponse the HTTP servlet response to write the expired cookie on
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthMessageDto> logout(HttpServletResponse servletResponse) {
        authService.clearAuthCookie(servletResponse);
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
