package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.collection.WishResponseDto;
import com.checkpoint.api.entities.Wish;

/**
 * Mapper for converting {@link Wish} entities to DTOs.
 */
public interface WishMapper {

    /**
     * Converts a Wish entity to a response DTO.
     *
     * @param wish the entity to convert
     * @return the response DTO
     */
    WishResponseDto toResponseDto(Wish wish);
}
