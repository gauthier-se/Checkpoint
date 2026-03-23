package com.checkpoint.api.events;

import java.util.UUID;

/**
 * Event published when a user creates a new review.
 * Used by the gamification system to award XP.
 */
public class ReviewCreatedEvent {

    private final UUID userId;

    /**
     * Constructs a new ReviewCreatedEvent.
     *
     * @param userId the ID of the user who created the review
     */
    public ReviewCreatedEvent(UUID userId) {
        this.userId = userId;
    }

    /**
     * Returns the ID of the user who created the review.
     *
     * @return the user ID
     */
    public UUID getUserId() {
        return userId;
    }
}
