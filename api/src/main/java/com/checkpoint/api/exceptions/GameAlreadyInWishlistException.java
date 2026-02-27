package com.checkpoint.api.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a game is already in the user's wishlist.
 */
public class GameAlreadyInWishlistException extends RuntimeException {

    private final UUID videoGameId;

    public GameAlreadyInWishlistException(UUID videoGameId) {
        super("Game already in wishlist with ID: " + videoGameId);
        this.videoGameId = videoGameId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
