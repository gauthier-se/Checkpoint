package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a user-game association already exists.
 */
public class GameAlreadyInLibraryException extends RuntimeException {

    private final UUID videoGameId;

    public GameAlreadyInLibraryException(UUID videoGameId) {
        super("Game already in library with ID: " + videoGameId);
        this.videoGameId = videoGameId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
