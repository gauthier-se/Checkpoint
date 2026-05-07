package com.checkpoint.api.dto.auth;

/**
 * Response DTO returned when a user with 2FA enabled completes the first login step.
 * The {@code intermediateToken} is only populated for Desktop clients; web clients
 * receive the token via the {@code checkpoint_2fa} HttpOnly cookie instead.
 */
public record TwoFactorRequiredResponseDto(
        boolean twoFactorRequired,
        String intermediateToken
) {}
