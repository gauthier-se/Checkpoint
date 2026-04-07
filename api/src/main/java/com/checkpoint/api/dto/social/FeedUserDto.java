package com.checkpoint.api.dto.social;

import java.util.UUID;

/**
 * Lightweight user info for activity feed items.
 *
 * @param id      the user ID
 * @param pseudo  the user's display name
 * @param picture the user's profile picture URL (nullable)
 */
public record FeedUserDto(
        UUID id,
        String pseudo,
        String picture
) {
}
