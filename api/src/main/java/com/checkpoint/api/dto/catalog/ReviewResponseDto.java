package com.checkpoint.api.dto.catalog;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a review returned to the client.
 */
public record ReviewResponseDto(
    UUID id,
    Integer score,
    String content,
    Boolean haveSpoilers,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    ReviewUserDto user
) {
}
