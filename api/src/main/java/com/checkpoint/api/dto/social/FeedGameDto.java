package com.checkpoint.api.dto.social;

import java.util.UUID;

/**
 * Lightweight game info for activity feed items.
 *
 * @param id       the video game ID
 * @param title    the game title
 * @param coverUrl the game cover image URL
 */
public record FeedGameDto(
        UUID id,
        String title,
        String coverUrl
) {
}
