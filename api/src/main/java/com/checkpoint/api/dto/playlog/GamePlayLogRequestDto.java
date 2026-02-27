package com.checkpoint.api.dto.playlog;

import java.time.LocalDate;
import java.util.UUID;

import com.checkpoint.api.enums.PlayStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating or updating a play log.
 *
 * @param platformId the platform ID
 * @param status     the play status (default ARE_PLAYING if null)
 * @param startDate  when the play session started
 * @param endDate    when the play session ended
 * @param timePlayed time played in minutes
 * @param ownership  ownership status (e.g. "owned", "borrowed")
 * @param isReplay   whether it's a replay (default false if null)
 */
public record GamePlayLogRequestDto(
        @NotNull(message = "Video Game ID is required")
        UUID videoGameId,
        @NotNull(message = "Platform ID is required")
        UUID platformId,
        PlayStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Integer timePlayed,
        String ownership,
        Boolean isReplay
) {}
