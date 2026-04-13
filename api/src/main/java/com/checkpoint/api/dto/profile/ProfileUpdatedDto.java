package com.checkpoint.api.dto.profile;

/**
 * DTO returned after a successful profile update.
 *
 * @param username  the updated display name
 * @param bio       the updated biography
 * @param picture   the profile picture URL
 * @param isPrivate whether the profile is private
 */
public record ProfileUpdatedDto(
        String username,
        String bio,
        String picture,
        Boolean isPrivate
) {}
