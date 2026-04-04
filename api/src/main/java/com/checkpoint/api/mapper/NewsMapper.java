package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.catalog.NewsResponseDto;
import com.checkpoint.api.entities.News;

/**
 * Mapper for {@link News} entities and DTOs.
 */
public interface NewsMapper {

    /**
     * Maps a News entity to a NewsResponseDto.
     *
     * @param news the news entity
     * @return the response DTO with flattened author information
     */
    NewsResponseDto toDto(News news);
}
