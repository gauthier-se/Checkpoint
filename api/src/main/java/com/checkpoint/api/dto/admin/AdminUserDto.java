package com.checkpoint.api.dto.admin;

import java.util.UUID;

/**
 * DTO for listing users in the admin panel.
 * Exposes only the minimal fields needed for the desktop admin table.
 *
 * @param id       the user's UUID
 * @param username the user's pseudo/username
 * @param email    the user's email address
 */
public record AdminUserDto(
        UUID id,
        String username,
        String email
) {}
