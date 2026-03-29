package com.checkpoint.api.dto.list;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a new game list.
 */
public record CreateGameListRequestDto(

        @NotBlank(message = "Title is required")
        String title,

        String description,

        Boolean isPrivate
) {}
