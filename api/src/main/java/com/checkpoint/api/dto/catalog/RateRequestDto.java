package com.checkpoint.api.dto.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating or updating a standalone rating.
 */
public record RateRequestDto(
    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    Integer score
) {
}
