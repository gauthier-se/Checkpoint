package com.checkpoint.api.dto.auth;

/**
 * Response body for Desktop authentication containing both tokens.
 *
 * @param accessToken  the short-lived JWT access token (24h)
 * @param refreshToken the long-lived opaque refresh token (7d)
 */
public record TokenPairDto(
        String accessToken,
        String refreshToken
) {}
