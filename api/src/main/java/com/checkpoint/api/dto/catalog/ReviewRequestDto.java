package com.checkpoint.api.dto.catalog;

/**
 * DTO for creating or updating a review.
 */
public record ReviewRequestDto(
    String content,

    Boolean haveSpoilers
) {
    public ReviewRequestDto {
        if (haveSpoilers == null) {
            haveSpoilers = false;
        }
    }
}
