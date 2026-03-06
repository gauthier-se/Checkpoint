package com.checkpoint.api.dto.catalog;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a rating returned to the client.
 */
public record RateResponseDto(
    UUID id,
    Integer score,
    UUID videoGameId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
