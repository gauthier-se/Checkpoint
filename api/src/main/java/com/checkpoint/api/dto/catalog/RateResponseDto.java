package com.checkpoint.api.dto.catalog;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a rating returned to the client.
 *
 * @param score rating score 1-10 (half-star steps; display = score / 2)
 */
public record RateResponseDto(
    UUID id,
    Integer score,
    UUID videoGameId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
