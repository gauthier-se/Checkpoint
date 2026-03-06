package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.dto.catalog.RateResponseDto;

/**
 * Service for managing standalone game ratings.
 */
public interface RateService {

    /**
     * Creates or updates the authenticated user's rating for a specific video game.
     *
     * @param userEmail the authenticated user's email
     * @param videoGameId the video game ID
     * @param score the rating score (1-5)
     * @return the created or updated rating
     */
    RateResponseDto rateGame(String userEmail, UUID videoGameId, Integer score);

    /**
     * Removes the authenticated user's rating for a specific video game.
     *
     * @param userEmail the authenticated user's email
     * @param videoGameId the video game ID
     */
    void removeRating(String userEmail, UUID videoGameId);

    /**
     * Retrieves the authenticated user's rating for a specific video game.
     *
     * @param userEmail the authenticated user's email
     * @param videoGameId the video game ID
     * @return the rating if found, null otherwise
     */
    RateResponseDto getUserRating(String userEmail, UUID videoGameId);
}
