package com.checkpoint.api.dto.list;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding a video game to a list.
 */
public record AddGameToListRequestDto(

        @NotNull(message = "Video game ID is required")
        UUID videoGameId
) {}
