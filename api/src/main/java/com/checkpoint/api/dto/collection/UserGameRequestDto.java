package com.checkpoint.api.dto.collection;

import java.util.UUID;

import com.checkpoint.api.enums.GameStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding a game to or updating a game in the user's collection.
 *
 * @param videoGameId the ID of the video game to add
 * @param status      the status to assign (BACKLOG, PLAYING, COMPLETED, DROPPED)
 */
public record UserGameRequestDto(
        @NotNull(message = "Video game ID is required") UUID videoGameId,
        @NotNull(message = "Status is required") GameStatus status
) {}
