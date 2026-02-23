package com.checkpoint.api.dto.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating or updating a review.
 */
public record ReviewRequestDto(
    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    Integer score,

    String content,

    Boolean haveSpoilers
) {
    public ReviewRequestDto {
        if (haveSpoilers == null) {
            haveSpoilers = false;
        }
    }
}
