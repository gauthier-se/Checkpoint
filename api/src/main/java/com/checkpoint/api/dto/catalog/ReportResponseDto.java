package com.checkpoint.api.dto.catalog;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a report returned to the client.
 *
 * @param id        the report ID
 * @param content   the report reason
 * @param createdAt the creation timestamp
 */
public record ReportResponseDto(
        UUID id,
        String content,
        LocalDateTime createdAt
) {}
