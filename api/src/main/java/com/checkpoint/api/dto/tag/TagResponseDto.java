package com.checkpoint.api.dto.tag;

import java.util.UUID;

/**
 * Response DTO for a tag with play log count.
 *
 * @param id            the tag ID
 * @param name          the tag name
 * @param playLogsCount the number of play logs associated with this tag
 */
public record TagResponseDto(
        UUID id,
        String name,
        long playLogsCount
) {}
