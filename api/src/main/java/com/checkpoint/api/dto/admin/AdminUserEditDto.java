package com.checkpoint.api.dto.admin;

/**
 * DTO for admin user profile editing operations.
 *
 * @param clearBio     if true, clears the user's bio
 * @param clearPicture if true, clears the user's profile picture
 * @param isPrivate    if provided, sets the user's profile visibility
 */
public record AdminUserEditDto(
        Boolean clearBio,
        Boolean clearPicture,
        Boolean isPrivate
) {}
