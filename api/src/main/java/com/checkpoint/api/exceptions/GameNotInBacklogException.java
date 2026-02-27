package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game is not found in the user's backlog.
 */
public class GameNotInBacklogException extends RuntimeException {

    private final UUID videoGameId;

    public GameNotInBacklogException(UUID videoGameId) {
        super("Game not found in backlog with ID: " + videoGameId);
        this.videoGameId = videoGameId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
