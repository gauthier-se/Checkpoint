package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game is not found in the user's wishlist.
 */
public class GameNotInWishlistException extends RuntimeException {

    private final UUID videoGameId;

    public GameNotInWishlistException(UUID videoGameId) {
        super("Game not found in wishlist with ID: " + videoGameId);
        this.videoGameId = videoGameId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
