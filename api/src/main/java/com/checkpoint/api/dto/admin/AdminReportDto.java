package com.checkpoint.api.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for carrying report summary details to the admin moderation dashboard.
 * Used in paginated report listings.
 */
public record AdminReportDto(
        UUID id,
        String reporterUsername,
        String reason,
        String type,
        String contentPreview,
        LocalDateTime createdAt
) {}
