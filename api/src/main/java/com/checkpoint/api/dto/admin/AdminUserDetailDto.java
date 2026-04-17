package com.checkpoint.api.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for displaying detailed user profile information in the admin panel.
 *
 * @param id          the user's UUID
 * @param username    the user's pseudo/username
 * @param email       the user's email address
 * @param bio         the user's bio text
 * @param picture     the user's profile picture URL
 * @param isPrivate   whether the user's profile is private
 * @param banned      whether the user is banned
 * @param xpPoint     the user's experience points
 * @param level       the user's level
 * @param createdAt   the account creation date
 * @param reviewCount the number of reviews written by the user
 * @param reportCount the number of reports received against the user's content
 */
public record AdminUserDetailDto(
        UUID id,
        String username,
        String email,
        String bio,
        String picture,
        Boolean isPrivate,
        Boolean banned,
        Integer xpPoint,
        Integer level,
        LocalDateTime createdAt,
        Long reviewCount,
        Long reportCount
) {}
