package com.checkpoint.api.dto.social;

import java.util.UUID;

/**
 * DTO representing a user in followers/following lists.
 *
 * @param id      the user's UUID
 * @param pseudo  the user's display name
 * @param picture the user's profile picture URL
 */
public record FollowUserDto(
        UUID id,
        String pseudo,
        String picture
) {}
