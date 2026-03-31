package com.checkpoint.api.dto.catalog;

import java.time.LocalDateTime;
import java.util.UUID;

import com.checkpoint.api.enums.PlayStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing a review returned to the client.
 * Play log context fields are excluded from the response when null (legacy reviews).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewResponseDto(
    UUID id,
    String content,
    Boolean haveSpoilers,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    ReviewUserDto user,
    UUID playLogId,
    String platformName,
    PlayStatus playStatus,
    Boolean isReplay,
    long likesCount,
    boolean hasLiked
) {
}
