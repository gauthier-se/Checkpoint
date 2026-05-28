package com.checkpoint.api.events;

import java.util.UUID;

/**
 * Event published after a user removes a game from their personal library.
 * Powers the {@code YOU_DIED} easter-egg badge.
 */
public class GameRemovedFromLibraryEvent {

    private final UUID userId;
    private final UUID videoGameId;

    public GameRemovedFromLibraryEvent(UUID userId, UUID videoGameId) {
        this.userId = userId;
        this.videoGameId = videoGameId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getVideoGameId() {
        return videoGameId;
    }
}
