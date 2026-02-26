package com.checkpoint.api.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for carrying review details to the admin moderation dashboard.
 */
public record AdminReviewDto(
        UUID id,
        String content,
        Boolean haveSpoilers,
        String authorUsername,
        String gameTitle,
        LocalDateTime createdAt
) {}
