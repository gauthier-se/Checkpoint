package com.checkpoint.api.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for carrying reported review details to the admin moderation dashboard.
 * Includes the number of reports filed against the review.
 */
public record AdminReportedReviewDto(
        UUID id,
        String content,
        String authorUsername,
        String gameTitle,
        long reportCount,
        LocalDateTime createdAt
) {}
