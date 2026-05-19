package com.checkpoint.api.dto.playlog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.checkpoint.api.dto.tag.TagSummaryDto;
import com.checkpoint.api.enums.PlayStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Full projection of a single play log, returned by {@code GET /api/plays/{id}}.
 * Optional fields are omitted from the response when null.
 *
 * @param id               the play log ID
 * @param createdAt        creation timestamp
 * @param updatedAt        last update timestamp
 * @param videoGameId      the played game ID
 * @param title            the game title
 * @param coverUrl         the game cover URL
 * @param releaseDate      the game release date
 * @param userId           the play log author ID
 * @param username         the author pseudo
 * @param userPicture      the author avatar URL
 * @param status           the play status
 * @param isReplay         whether this play is a replay
 * @param timePlayed       time played in minutes
 * @param startDate        when the session started
 * @param endDate          when the session ended
 * @param ownership        ownership status (e.g. "owned", "borrowed")
 * @param platformId       the platform ID
 * @param platformName     the platform name
 * @param score            rating 1-10 (half-star steps; display = score / 2)
 * @param tags             tags attached to this play log
 * @param review           review nested summary, or null if no review
 * @param isOwner          true when the authenticated viewer is the author
 * @param isLikedByViewer  whether the viewer has liked the game (top-level like)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayLogDetailDto(
        UUID id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID videoGameId,
        String title,
        String coverUrl,
        LocalDate releaseDate,
        UUID userId,
        String username,
        String userPicture,
        PlayStatus status,
        Boolean isReplay,
        Integer timePlayed,
        LocalDate startDate,
        LocalDate endDate,
        String ownership,
        UUID platformId,
        String platformName,
        Integer score,
        List<TagSummaryDto> tags,
        ReviewSummaryDto review,
        Boolean isOwner,
        Boolean isLikedByViewer
) {}
