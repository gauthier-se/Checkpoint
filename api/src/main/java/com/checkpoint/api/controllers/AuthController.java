package com.checkpoint.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.api.dto.auth.AuthMessageDto;
import com.checkpoint.api.dto.auth.LoginRequestDto;
import com.checkpoint.api.dto.auth.LoginResponseDto;
import com.checkpoint.api.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Hybrid authentication controller.
 *
 * <p>Provides a unified login endpoint that returns the appropriate credential type
 * based on the client:</p>
 * <ul>
 *   <li><strong>Desktop</strong> ({@code X-Client-Type: Desktop} header or
 *       {@code POST /api/auth/token}): returns a JWT in the response body.</li>
 *   <li><strong>Web</strong> (default): creates a server-side session with a
 *       {@code JSESSIONID} cookie.</li>
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
     * <p>If the {@code X-Client-Type} header is set to {@code Desktop},
     * a JWT token is returned. Otherwise, a server-side session is created.</p>
     *
     * @param loginRequest    the login credentials
     * @param clientType      optional header to specify the client type
     * @param servletRequest  the HTTP servlet request
     * @param servletResponse the HTTP servlet response
     * @return JWT token (Desktop) or success message (Web)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            @org.springframework.web.bind.annotation.RequestHeader(
                    value = "X-Client-Type", required = false) String clientType,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        if ("Desktop".equalsIgnoreCase(clientType)) {
            String token = authService.authenticateAndGenerateToken(loginRequest);
            return ResponseEntity.ok(new LoginResponseDto(token));
        }

        authService.authenticateAndCreateSession(loginRequest, servletRequest, servletResponse);
        return ResponseEntity.ok(new AuthMessageDto("Login successful"));
    }

    /**
     * Dedicated JWT token endpoint for Desktop clients.
     *
     * <p>Always returns a JWT token regardless of headers.</p>
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
     * <p>For Web clients, invalidates the server-side session.
     * For Desktop/JWT clients, this is effectively a no-op (JWT is stateless).
     * Token blacklisting can be added in the future.</p>
     *
     * @param servletRequest  the HTTP servlet request
     * @param servletResponse the HTTP servlet response
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthMessageDto> logout(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        authService.logoutSession(servletRequest, servletResponse);
        return ResponseEntity.ok(new AuthMessageDto("Logout successful"));
    }
}
