package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.catalog.RateResponseDto;
import com.checkpoint.api.entities.Rate;

/**
 * Mapper for converting Rate entities to DTOs.
 */
public interface RateMapper {

    /**
     * Maps a Rate entity to a RateResponseDto.
     *
     * @param rate the rate entity
     * @return the rate response DTO
     */
    RateResponseDto toDto(Rate rate);
}
