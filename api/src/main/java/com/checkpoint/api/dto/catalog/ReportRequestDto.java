package com.checkpoint.api.dto.catalog;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a report on a review.
 *
 * @param content the reason for reporting (required)
 */
public record ReportRequestDto(
        @NotBlank(message = "Content is required") String content
) {}
