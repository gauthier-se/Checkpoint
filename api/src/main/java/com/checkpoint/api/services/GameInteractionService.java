package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.dto.collection.GameInteractionStatusDto;

/**
 * Service for aggregating user interactions with games.
 */
public interface GameInteractionService {

    /**
     * Retrieves the aggregate interaction status for a specific user and game.
     *
     * @param userEmail   the email of the current user
     * @param videoGameId the ID of the video game
     * @return an aggregate DTO representing the user's interaction state
     */
    GameInteractionStatusDto getGameInteractionStatus(String userEmail, UUID videoGameId);
}
