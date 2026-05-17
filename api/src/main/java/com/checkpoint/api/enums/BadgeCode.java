package com.checkpoint.api.enums;

/**
 * Stable catalog of badges that can be awarded automatically by the platform.
 *
 * <p>Each constant carries a default display name and description used by the
 * database seeder when creating the corresponding {@code badges} row. The enum
 * name (e.g. {@code FIRST_REVIEW}) is the immutable contract between in-code
 * logic and the persisted {@code Badge.code} column — admins may later edit
 * the display name/description in the DB without breaking awarding.
 */
public enum BadgeCode {

    FIRST_REVIEW(
            "First Review",
            "Awarded for writing your very first review."),
    REVIEW_10(
            "Critic in the Making",
            "Awarded for writing 10 reviews."),
    REVIEW_50(
            "Seasoned Critic",
            "Awarded for writing 50 reviews."),
    FIRST_GAME_FINISHED(
            "First Completion",
            "Awarded for marking your first game as completed."),
    GAME_FINISHED_10(
            "Dedicated Player",
            "Awarded for completing 10 games."),
    GAME_FINISHED_50(
            "Backlog Slayer",
            "Awarded for completing 50 games."),
    LEVEL_5(
            "Rising Star",
            "Awarded for reaching level 5."),
    LEVEL_10(
            "Pro Tracker",
            "Awarded for reaching level 10."),
    LEVEL_25(
            "Legendary",
            "Awarded for reaching level 25.");

    private final String defaultName;
    private final String defaultDescription;

    BadgeCode(String defaultName, String defaultDescription) {
        this.defaultName = defaultName;
        this.defaultDescription = defaultDescription;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }
}
