package com.checkpoint.api.dto.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating or updating a standalone rating.
 *
 * @param score rating score 1-10 (half-star steps; 1 = 0.5★, 10 = 5★)
 */
public record RateRequestDto(
    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 10, message = "Score must be at most 10")
    Integer score
) {
}
