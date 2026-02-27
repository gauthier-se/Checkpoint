package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game is already in the user's backlog.
 */
public class GameAlreadyInBacklogException extends RuntimeException {

    private final UUID videoGameId;

    public GameAlreadyInBacklogException(UUID videoGameId) {
        super("Game already in backlog with ID: " + videoGameId);
        this.videoGameId = videoGameId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
