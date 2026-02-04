package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game is not found in the local database.
 */
public class GameNotFoundException extends RuntimeException {

    private final UUID gameId;

    public GameNotFoundException(UUID gameId) {
        super("Game not found with ID: " + gameId);
        this.gameId = gameId;
    }

    public UUID getGameId() {
        return gameId;
    }
}
