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
     * {@code GAME_FINISHED_50}, {@code BACKLOG_HUNDRED}).
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

    /**
     * Evaluates play-log thresholds and awards any newly eligible badges
     * ({@code CENTURION}, {@code MULTIPLATFORM_NOMAD}).
     *
     * @param userId the ID of the user
     */
    void checkPlayLogBadges(UUID userId);

    /**
     * Evaluates library-size thresholds and awards any newly eligible badges
     * ({@code LIBRARY_50}, {@code LIBRARY_200}).
     *
     * @param userId the ID of the user
     */
    void checkLibrarySizeBadges(UUID userId);

    /**
     * Evaluates genre-completion thresholds and awards any newly eligible badges
     * ({@code RPG_DISCIPLE}, {@code SHOOTER_TRIGGER_HAPPY}, {@code PLATFORMER_HERO},
     * {@code INDIE_GEM_HUNTER}).
     *
     * @param userId the ID of the user
     */
    void checkGenreBadges(UUID userId);

    /**
     * Evaluates review-quality thresholds and awards any newly eligible badges
     * ({@code FIVE_STAR_STREAK}, {@code BRUTAL_CRITIC}, {@code WORDSMITH}).
     *
     * @param userId the ID of the user
     */
    void checkReviewQualityBadges(UUID userId);

    /**
     * Evaluates social-interaction thresholds and awards any newly eligible badges
     * ({@code NETWORKER}, {@code CHARISMATIC}, {@code PRAISE_THE_SUN},
     * {@code BELOVED_REVIEWER}).
     *
     * @param userId the ID of the user whose social counts to evaluate
     */
    void checkSocialBadges(UUID userId);

    /**
     * Evaluates longevity thresholds and awards any newly eligible badges
     * ({@code VETERAN_30}, {@code LIFER}) based on the user's registration date.
     *
     * @param userId the ID of the user
     */
    void checkLongevityBadges(UUID userId);

    /**
     * Evaluates rating-driven easter-egg badges ({@code THE_CAKE_IS_A_LIE},
     * {@code INDECISIVE}). Called on every rating create or update.
     *
     * @param userId the ID of the user
     */
    void checkRatingBadges(UUID userId);

    /**
     * Awards the {@code YOU_DIED} easter-egg badge. Called whenever a user
     * removes a game from their library.
     *
     * @param userId the ID of the user
     */
    void checkGameRemovedBadges(UUID userId);

    /**
     * Evaluates the {@code MISSION_FAILED} easter-egg badge: awarded when the
     * user deletes a review within five minutes of posting it.
     *
     * @param userId          the ID of the user
     * @param reviewCreatedAt the timestamp the deleted review was created
     */
    void checkReviewDeletedBadges(UUID userId, java.time.LocalDateTime reviewCreatedAt);

    /**
     * Evaluates the {@code LEEROY} easter-egg badge: awarded when the user marks
     * a game as PLAYING while their backlog already holds 20+ entries.
     *
     * @param userId the ID of the user
     */
    void checkGameStartedBadges(UUID userId);

    /**
     * Evaluates the {@code WAKE_UP_MR_FREEMAN} easter-egg badge: awarded when
     * the user logs activity after a long absence.
     *
     * @param userId   the ID of the user
     * @param daysAway the gap, in days, since the user's previous activity
     */
    void checkReturningUserBadges(UUID userId, long daysAway);

    /**
     * Sweeps every user with a backlog entry that has been sitting for at least
     * a year and awards them the {@code SNAKE_BACKLOG} easter-egg badge. Driven
     * by a nightly scheduled job.
     */
    void awardLongBacklogUsers();

    /**
     * Evaluates the {@code STAY_AWHILE_REVIEWS} easter-egg badge: awarded once
     * the user has opened 50 distinct reviews from other users.
     *
     * @param userId the ID of the user
     */
    void checkReaderBadges(UUID userId);
}
