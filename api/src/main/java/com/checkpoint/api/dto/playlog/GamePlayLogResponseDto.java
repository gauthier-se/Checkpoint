package com.checkpoint.api.dto.playlog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.checkpoint.api.dto.tag.TagSummaryDto;
import com.checkpoint.api.enums.PlayStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for a play log entry.
 *
 * @param id            the play log ID
 * @param videoGameId   the video game ID
 * @param title         the video game title
 * @param coverUrl      the video game cover image URL
 * @param platformId    the platform ID
 * @param platformName  the platform name
 * @param status        the play status
 * @param isReplay      whether it's a replay
 * @param timePlayed    time played in minutes
 * @param startDate     when the play session started
 * @param endDate       when the play session ended
 * @param ownership     ownership status
 * @param createdAt     creation timestamp
 * @param updatedAt     last update timestamp
 * @param hasReview     whether this play log has a review attached
 * @param reviewPreview first 100 characters of the review content, or null
 * @param score         rating score (1-5) given during this play session, or null
 * @param tags          list of tags associated with this play log
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        LocalDateTime updatedAt,
        Boolean hasReview,
        String reviewPreview,
        Integer score,
        List<TagSummaryDto> tags
) {}
