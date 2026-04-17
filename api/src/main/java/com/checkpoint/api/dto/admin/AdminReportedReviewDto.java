package com.checkpoint.api.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for carrying reported review details to the admin moderation dashboard.
 * Includes the number of reports filed against the review and the author's identifier so
 * admin tooling can navigate to the author's profile or perform moderation on them directly.
 */
public record AdminReportedReviewDto(
        UUID id,
        String content,
        UUID authorId,
        String authorUsername,
        String gameTitle,
        long reportCount,
        LocalDateTime createdAt
) {}
