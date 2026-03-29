package com.checkpoint.api.dto.list;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for game list detail view.
 * Includes all card fields plus entries, ownership, and like status.
 */
public record GameListDetailDto(
        UUID id,
        String title,
        String description,
        Boolean isPrivate,
        Integer videoGamesCount,
        Long likesCount,
        String authorPseudo,
        String authorPicture,
        List<GameListEntryDto> entries,
        Boolean isOwner,
        Boolean hasLiked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
