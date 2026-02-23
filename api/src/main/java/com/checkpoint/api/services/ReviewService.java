package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;

/**
 * Service for managing game reviews.
 */
public interface ReviewService {

    /**
     * Adds or updates a review for a specific video game.
     *
     * @param pseudo the authenticated user's pseudo
     * @param videoGameId the video game ID
     * @param request the review request
     * @return the created or updated review
     */
    ReviewResponseDto addOrUpdateReview(String pseudo, UUID videoGameId, ReviewRequestDto request);

    /**
     * Retrieves a paginated list of reviews for a specific video game.
     *
     * @param videoGameId the video game ID
     * @param pageable pagination and sorting details
     * @return a page of reviews
     */
    Page<ReviewResponseDto> getGameReviews(UUID videoGameId, Pageable pageable);

    /**
     * Deletes the authenticated user's review for a specific video game.
     *
     * @param pseudo the authenticated user's pseudo
     * @param videoGameId the video game ID
     */
    void deleteReview(String pseudo, UUID videoGameId);
}
