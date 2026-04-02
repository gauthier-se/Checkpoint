package com.checkpoint.api.dto.list;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for game list cards (browse/listing views).
 */
public record GameListCardDto(
        UUID id,
        String title,
        String description,
        Boolean isPrivate,
        Integer videoGamesCount,
        Long likesCount,
        Long commentsCount,
        String authorPseudo,
        String authorPicture,
        List<String> coverUrls,
        LocalDateTime createdAt
) {}
