package com.checkpoint.api.mapper;

import com.checkpoint.api.dto.catalog.ReviewResponseDto;
import com.checkpoint.api.entities.Review;

/**
 * Mapper for converting Review entities to DTOs.
 */
public interface ReviewMapper {

    /**
     * Maps a Review entity to a ReviewResponseDto with default like values (0, false).
     *
     * @param review the review entity
     * @return the review response DTO
     */
    ReviewResponseDto toDto(Review review);

    /**
     * Maps a Review entity to a ReviewResponseDto with like context.
     *
     * @param review     the review entity
     * @param likesCount the number of likes on this review
     * @param hasLiked   whether the current viewer has liked this review
     * @return the review response DTO
     */
    ReviewResponseDto toDto(Review review, long likesCount, boolean hasLiked);
}
