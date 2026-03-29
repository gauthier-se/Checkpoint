package com.checkpoint.api.dto.list;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for reordering games in a list.
 * The position is inferred from the index in the list.
 */
public record ReorderGamesRequestDto(

        @NotNull(message = "Ordered video game IDs are required")
        List<UUID> orderedVideoGameIds
) {}
