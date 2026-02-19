package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game is not found in the user's library.
 */
public class GameNotInLibraryException extends RuntimeException {

    private final UUID videoGameId;

    public GameNotInLibraryException(UUID videoGameId) {
        super("Game not found in library with ID: " + videoGameId);
        this.videoGameId = videoGameId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
