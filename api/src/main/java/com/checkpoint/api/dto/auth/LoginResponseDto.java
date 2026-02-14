package com.checkpoint.api.dto.auth;

/**
 * Response body for Desktop JWT login.
 * Contains the JWT token to be used in subsequent API calls.
 *
 * @param token the JWT access token
 */
public record LoginResponseDto(
        String token
) {}
