package com.checkpoint.api.events;

import java.util.UUID;

import com.checkpoint.api.enums.BadgeCode;

/**
 * Event published when a badge has been freshly awarded to a user.
 * Downstream consumers (e.g. notifications) can react to it.
 */
public class BadgeUnlockedEvent {

    private final UUID userId;
    private final BadgeCode code;

    /**
     * Constructs a new BadgeUnlockedEvent.
     *
     * @param userId the ID of the user who unlocked the badge
     * @param code   the catalog code of the unlocked badge
     */
    public BadgeUnlockedEvent(UUID userId, BadgeCode code) {
        this.userId = userId;
        this.code = code;
    }

    /**
     * Returns the ID of the user who unlocked the badge.
     *
     * @return the user ID
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Returns the catalog code of the unlocked badge.
     *
     * @return the badge code
     */
    public BadgeCode getCode() {
        return code;
    }
}
