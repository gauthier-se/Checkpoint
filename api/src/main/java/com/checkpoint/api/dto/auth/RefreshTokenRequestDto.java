package com.checkpoint.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for desktop refresh token exchange.
 *
 * @param refreshToken the refresh token issued at login
 */
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token is required") String refreshToken
) {}
