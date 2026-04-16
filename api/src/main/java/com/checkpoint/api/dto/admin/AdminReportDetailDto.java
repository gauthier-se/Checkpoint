package com.checkpoint.api.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for carrying full report details to the admin moderation dashboard.
 * Includes the reported content and its author information.
 */
public record AdminReportDetailDto(
        UUID id,
        String reporterUsername,
        String reason,
        String type,
        UUID targetId,
        String targetAuthorUsername,
        String targetFullContent,
        LocalDateTime createdAt
) {}
