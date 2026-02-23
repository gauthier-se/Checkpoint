package com.checkpoint.api.dto.catalog;

import java.util.UUID;

/**
 * DTO containing basic user information for a review.
 */
public record ReviewUserDto(
    UUID id,
    String pseudo,
    String picture
) {
}
