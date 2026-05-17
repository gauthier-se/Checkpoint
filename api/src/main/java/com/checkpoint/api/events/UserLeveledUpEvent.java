package com.checkpoint.api.events;

import java.util.UUID;

/**
 * Event published when a user's level increases.
 * Used by the badge system to evaluate level-based badge criteria.
 */
public class UserLeveledUpEvent {

    private final UUID userId;
    private final int newLevel;

    /**
     * Constructs a new UserLeveledUpEvent.
     *
     * @param userId   the ID of the user who leveled up
     * @param newLevel the level the user just reached
     */
    public UserLeveledUpEvent(UUID userId, int newLevel) {
        this.userId = userId;
        this.newLevel = newLevel;
    }

    /**
     * Returns the ID of the user who leveled up.
     *
     * @return the user ID
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Returns the new level the user reached.
     *
     * @return the new level
     */
    public int getNewLevel() {
        return newLevel;
    }
}
