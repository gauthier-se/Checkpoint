package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.tag.TagResponseDto;
import com.checkpoint.api.dto.tag.TagSummaryDto;
import com.checkpoint.api.entities.Tag;

/**
 * Mapper for {@link Tag} entities and DTOs.
 */
public interface TagMapper {

    /**
     * Maps a Tag entity to a TagResponseDto including play log count.
     *
     * @param tag           the tag entity
     * @param playLogsCount the number of play logs associated with this tag
     * @return the response DTO
     */
    TagResponseDto toDto(Tag tag, long playLogsCount);

    /**
     * Maps a Tag entity to a lightweight TagSummaryDto.
     *
     * @param tag the tag entity
     * @return the summary DTO
     */
    TagSummaryDto toSummaryDto(Tag tag);
}
