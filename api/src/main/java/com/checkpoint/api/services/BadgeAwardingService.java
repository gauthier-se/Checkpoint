package com.checkpoint.api.services;

import java.util.UUID;

import com.checkpoint.api.enums.BadgeCode;

/**
 * Service responsible for awarding badges to users when criteria are met.
 *
 * <p>All methods are idempotent: awarding a badge a user already owns is a no-op.</p>
 */
public interface BadgeAwardingService {

    /**
     * Awards the given badge to the user if they do not already own it.
     *
     * @param userId the ID of the user
     * @param code   the catalog code of the badge to award
     */
    void awardIfEligible(UUID userId, BadgeCode code);

    /**
     * Evaluates review-count thresholds for the user and awards any newly
     * eligible review badges ({@code FIRST_REVIEW}, {@code REVIEW_10}, {@code REVIEW_50}).
     *
     * @param userId the ID of the user
     */
    void checkReviewBadges(UUID userId);

    /**
     * Evaluates game-completion thresholds for the user and awards any newly
     * eligible badges ({@code FIRST_GAME_FINISHED}, {@code GAME_FINISHED_10},
     * {@code GAME_FINISHED_50}).
     *
     * @param userId the ID of the user
     */
    void checkGameFinishedBadges(UUID userId);

    /**
     * Evaluates level thresholds and awards any newly eligible level badges
     * ({@code LEVEL_5}, {@code LEVEL_10}, {@code LEVEL_25}).
     *
     * @param userId   the ID of the user
     * @param newLevel the level the user just reached
     */
    void checkLevelBadges(UUID userId, int newLevel);
}
