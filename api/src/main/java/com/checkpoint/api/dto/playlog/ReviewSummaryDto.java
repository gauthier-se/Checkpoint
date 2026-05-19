package com.checkpoint.api.dto.playlog;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Lightweight review projection nested in {@link PlayLogDetailDto}.
 *
 * @param id              the review ID
 * @param content         the review text
 * @param haveSpoilers    whether the review contains spoilers
 * @param createdAt       creation timestamp
 * @param updatedAt       last update timestamp
 * @param likeCount       number of likes on this review
 * @param commentCount    number of comments on this review
 * @param isLikedByViewer whether the authenticated viewer has liked the review (null if anonymous)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewSummaryDto(
        UUID id,
        String content,
        Boolean haveSpoilers,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long likeCount,
        Long commentCount,
        Boolean isLikedByViewer
) {}
