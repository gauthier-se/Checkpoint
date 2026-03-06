package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a rating is not found for a specific user and game.
 */
public class RateNotFoundException extends RuntimeException {

    public RateNotFoundException(UUID videoGameId) {
        super("Rating not found for game with ID: " + videoGameId);
    }
}
