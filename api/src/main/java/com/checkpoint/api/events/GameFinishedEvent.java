package com.checkpoint.api.events;

import java.util.UUID;

/**
 * Event published when a user marks a game as completed.
 * Used by the gamification system to award XP.
 */
public class GameFinishedEvent {

    private final UUID userId;

    /**
     * Constructs a new GameFinishedEvent.
     *
     * @param userId the ID of the user who finished the game
     */
    public GameFinishedEvent(UUID userId) {
        this.userId = userId;
    }

    /**
     * Returns the ID of the user who finished the game.
     *
     * @return the user ID
     */
    public UUID getUserId() {
        return userId;
    }
}
