package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game is already present in a list.
 */
public class GameAlreadyInListException extends RuntimeException {

    private final UUID videoGameId;

    public GameAlreadyInListException(UUID videoGameId) {
        super("Game already in list with ID: " + videoGameId);
        this.videoGameId = videoGameId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
