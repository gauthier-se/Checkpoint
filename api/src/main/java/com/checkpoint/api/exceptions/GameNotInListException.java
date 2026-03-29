package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game is not found in a list.
 */
public class GameNotInListException extends RuntimeException {

    private final UUID videoGameId;

    public GameNotInListException(UUID videoGameId) {
        super("Game not in list with ID: " + videoGameId);
        this.videoGameId = videoGameId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
