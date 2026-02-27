package com.checkpoint.api.dto.playlog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.checkpoint.api.enums.PlayStatus;

/**
 * Response DTO for a play log entry.
 *
 * @param id           the play log ID
 * @param videoGameId  the video game ID
 * @param title        the video game title
 * @param coverUrl     the video game cover image URL
 * @param platformId   the platform ID
 * @param platformName the platform name
 * @param status       the play status
 * @param isReplay     whether it's a replay
 * @param timePlayed   time played in minutes
 * @param startDate    when the play session started
 * @param endDate      when the play session ended
 * @param ownership    ownership status
 * @param createdAt    creation timestamp
 * @param updatedAt    last update timestamp
 */
public record GamePlayLogResponseDto(
        UUID id,
        UUID videoGameId,
        String title,
        String coverUrl,
        UUID platformId,
        String platformName,
        PlayStatus status,
        Boolean isReplay,
        Integer timePlayed,
        LocalDate startDate,
        LocalDate endDate,
        String ownership,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
