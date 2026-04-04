package com.checkpoint.api.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a tag.
 *
 * @param name the tag name (max 50 characters, will be normalized to lowercase)
 */
public record TagRequestDto(
        @NotBlank(message = "Tag name is required")
        @Size(max = 50, message = "Tag name must be at most 50 characters")
        String name
) {}
