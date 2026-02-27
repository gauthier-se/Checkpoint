package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.collection.BacklogResponseDto;
import com.checkpoint.api.entities.Backlog;

/**
 * Mapper for converting {@link Backlog} entities to DTOs.
 */
public interface BacklogMapper {

    /**
     * Converts a Backlog entity to a response DTO.
     *
     * @param backlog the entity to convert
     * @return the response DTO
     */
    BacklogResponseDto toResponseDto(Backlog backlog);
}
