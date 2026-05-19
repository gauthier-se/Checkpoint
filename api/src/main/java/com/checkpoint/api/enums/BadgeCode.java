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
            "Awarded for reaching level 25."),

    // Completion & milestones
    BACKLOG_HUNDRED(
            "Backlog Vanquished",
            "Awarded for finishing 100 games."),
    CENTURION(
            "Centurion",
            "Awarded for logging 100 plays."),
    LIBRARY_50(
            "Library Builder",
            "Awarded for reaching 50 games in your library."),
    LIBRARY_200(
            "Bigger on the Inside",
            "Awarded for reaching 200 games in your library."),

    // Genre / platform mastery
    RPG_DISCIPLE(
            "Stay Awhile and Listen",
            "Awarded for finishing 10 RPGs."),
    SHOOTER_TRIGGER_HAPPY(
            "Trigger Happy",
            "Awarded for finishing 10 shooters."),
    PLATFORMER_HERO(
            "The Princess Is in Another Castle",
            "Awarded for finishing 10 platformers."),
    INDIE_GEM_HUNTER(
            "Hidden Gem Hunter",
            "Awarded for finishing 20 indie games."),
    MULTIPLATFORM_NOMAD(
            "Did You Win Yet?",
            "Awarded for logging games from 5 or more different platforms."),

    // Reviews
    FIVE_STAR_STREAK(
            "It's Super Effective!",
            "Awarded for writing three consecutive 5-star reviews."),
    BRUTAL_CRITIC(
            "Press F to Pay Respects",
            "Awarded for writing ten 1-star reviews."),
    WORDSMITH(
            "Wall of Text",
            "Awarded for writing a review of 1000 or more characters."),
    BELOVED_REVIEWER(
            "Critic Beloved",
            "Awarded for receiving 50 likes on your reviews."),

    // Social
    NETWORKER(
            "Would You Kindly?",
            "Awarded for following 10 users."),
    CHARISMATIC(
            "Yo Niko, Let's Go Bowling",
            "Awarded for being followed by 10 users."),
    PRAISE_THE_SUN(
            "Praise the Sun",
            "Awarded for giving 100 likes."),

    // Longevity
    VETERAN_30(
            "I Used to Be an Adventurer Like You",
            "Awarded for staying active 30 days since registration."),
    LIFER(
            "The Cake Is a Lie",
            "Awarded for staying active 365 days since registration.");

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
