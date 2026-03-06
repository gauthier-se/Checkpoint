package com.checkpoint.api.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.checkpoint.api.dto.catalog.ReviewRequestDto;
import com.checkpoint.api.dto.catalog.ReviewResponseDto;

/**
 * Service for managing game reviews.
 * Reviews are tied to play log entries, allowing multiple reviews per game (one per playthrough).
 */
public interface ReviewService {

    /**
     * Retrieves a paginated list of all reviews for a specific video game.
     * Includes reviews from all users and all their play logs.
     *
     * @param videoGameId the video game ID
     * @param pageable    pagination and sorting details
     * @return a page of reviews
     */
    Page<ReviewResponseDto> getGameReviews(UUID videoGameId, Pageable pageable);

    /**
     * Creates a review attached to a specific play log entry.
     *
     * @param userEmail the authenticated user's email
     * @param playId    the play log ID
     * @param request   the review request containing content and spoiler flag
     * @return the created review
     * @throws com.checkpoint.api.exceptions.PlayLogNotFoundException      if the play log does not exist or does not belong to the user
     * @throws com.checkpoint.api.exceptions.ReviewAlreadyExistsException  if the play log already has a review
     */
    ReviewResponseDto createPlayLogReview(String userEmail, UUID playId, ReviewRequestDto request);

    /**
     * Updates the review of a specific play log entry.
     *
     * @param userEmail the authenticated user's email
     * @param playId    the play log ID
     * @param request   the review request containing updated content and spoiler flag
     * @return the updated review
     * @throws com.checkpoint.api.exceptions.PlayLogNotFoundException  if the play log does not exist or does not belong to the user
     * @throws com.checkpoint.api.exceptions.ReviewNotFoundException   if no review exists for the play log
     */
    ReviewResponseDto updatePlayLogReview(String userEmail, UUID playId, ReviewRequestDto request);

    /**
     * Deletes the review of a specific play log entry.
     *
     * @param userEmail the authenticated user's email
     * @param playId    the play log ID
     * @throws com.checkpoint.api.exceptions.PlayLogNotFoundException  if the play log does not exist or does not belong to the user
     * @throws com.checkpoint.api.exceptions.ReviewNotFoundException   if no review exists for the play log
     */
    void deletePlayLogReview(String userEmail, UUID playId);

    /**
     * Retrieves the review of a specific play log entry.
     *
     * @param userEmail the authenticated user's email
     * @param playId    the play log ID
     * @return the review
     * @throws com.checkpoint.api.exceptions.PlayLogNotFoundException  if the play log does not exist or does not belong to the user
     * @throws com.checkpoint.api.exceptions.ReviewNotFoundException   if no review exists for the play log
     */
    ReviewResponseDto getPlayLogReview(String userEmail, UUID playId);
}
