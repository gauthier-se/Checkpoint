package com.checkpoint.api.dto.list;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a single entry in a game list.
 */
public record GameListEntryDto(
        UUID videoGameId,
        String title,
        String coverUrl,
        LocalDate releaseDate,
        Integer position,
        LocalDateTime addedAt
) {}
