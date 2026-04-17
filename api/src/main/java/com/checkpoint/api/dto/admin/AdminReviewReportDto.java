package com.checkpoint.api.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object representing a single report filed against a review,
 * used in the admin moderation dashboard when inspecting a specific review's reports.
 */
public record AdminReviewReportDto(
        UUID id,
        String reporterUsername,
        String reason,
        LocalDateTime createdAt
) {}
