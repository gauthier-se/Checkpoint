package com.checkpoint.api.dto.export;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.checkpoint.api.enums.PlayStatus;

/**
 * Exported play log row.
 *
 * @param score rating score 1-10 (half-star steps; display = score / 2), or null
 */
public record PlayLogExport(
        UUID id,
        UUID gameId,
        String gameTitle,
        PlayStatus status,
        Boolean isReplay,
        Integer timePlayed,
        LocalDate startDate,
        LocalDate endDate,
        String ownership,
        Integer score,
        String platform,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
