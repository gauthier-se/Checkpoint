package com.checkpoint.api.dto.collection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.checkpoint.api.enums.Priority;

/**
 * Response DTO for a game in the user's wishlist.
 *
 * @param id          the wish association ID
 * @param videoGameId the video game ID
 * @param title       the video game title
 * @param coverUrl    the video game cover image URL
 * @param releaseDate the video game release date
 * @param priority    the user-assigned priority, or {@code null} when none
 * @param addedAt     when the game was added to the wishlist
 */
public record WishResponseDto(
        UUID id,
        UUID videoGameId,
        String title,
        String coverUrl,
        LocalDate releaseDate,
        Priority priority,
        LocalDateTime addedAt
) {}
