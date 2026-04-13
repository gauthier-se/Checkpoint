package com.checkpoint.api.dto.auth;

import java.util.UUID;

/**
 * DTO for the /api/auth/me endpoint response.
 * Returns basic profile information of the currently authenticated user.
 *
 * @param id        the user's UUID
 * @param username  the user's pseudo/username
 * @param email     the user's email address
 * @param role      the user's role name (e.g., "ADMIN", "USER")
 * @param bio       the user's biography
 * @param picture   the user's profile picture URL
 * @param isPrivate whether the user's profile is private
 */
public record UserMeDto(
        UUID id,
        String username,
        String email,
        String role,
        String bio,
        String picture,
        Boolean isPrivate
) {}
