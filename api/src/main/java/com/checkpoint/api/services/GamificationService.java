package com.checkpoint.api.services;

import java.util.UUID;

/**
 * Service responsible for managing user XP and leveling.
 */
public interface GamificationService {

    /**
     * Adds XP to a user and handles level-up if the XP threshold is reached.
     * The level-up threshold is calculated as {@code level * 1000}.
     *
     * @param userId   the ID of the user to award XP to
     * @param xpAmount the amount of XP to add
     */
    void addXp(UUID userId, int xpAmount);
}
