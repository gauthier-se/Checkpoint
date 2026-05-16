package com.checkpoint.api.dto.export;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Exported rating row.
 *
 * @param score rating score 1-10 (half-star steps; display = score / 2)
 */
public record RatingExport(
        UUID gameId,
        String gameTitle,
        Integer score,
        LocalDateTime createdAt) {
}
