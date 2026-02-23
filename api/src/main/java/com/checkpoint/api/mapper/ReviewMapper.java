package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.entities.Review;

/**
 * Mapper for converting Review entities to DTOs.
 */
public interface ReviewMapper {

    /**
     * Maps a Review entity and its associated score to a ReviewResponseDto.
     *
     * @param review the review entity
     * @param score the numeric score
     * @return the review response DTO
     */
    ReviewResponseDto toDto(Review review, Integer score);
}
