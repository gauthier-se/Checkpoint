package com.checkpoint.api.dto.tag;

import java.util.UUID;

/**
 * Lightweight tag DTO for embedding in play log responses.
 *
 * @param id   the tag ID
 * @param name the tag name
 */
public record TagSummaryDto(
        UUID id,
        String name
) {}
