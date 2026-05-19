package com.checkpoint.api.events;

import java.util.UUID;

/**
 * Event published when a user logs a new play (a new {@code UserGamePlay} row).
 * Used by the gamification system to award badges that depend on play counts,
 * library size, and platform diversity.
 */
public class PlayLogCreatedEvent {

    private final UUID userId;

    /**
     * Constructs a new PlayLogCreatedEvent.
     *
     * @param userId the ID of the user who logged the play
     */
    public PlayLogCreatedEvent(UUID userId) {
        this.userId = userId;
    }

    /**
     * Returns the ID of the user who logged the play.
     *
     * @return the user ID
     */
    public UUID getUserId() {
        return userId;
    }
}
